package org.ksharp.parser.ksharp

import org.ksharp.common.*
import org.ksharp.nodes.ModuleNode
import org.ksharp.nodes.NodeData
import org.ksharp.parser.*
import java.io.File
import java.io.Reader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

typealias KSharpParserResult = ParserResult<NodeData, KSharpLexerState>
typealias KSharpConsumeResult = ConsumeResult<KSharpLexerState>

fun KSharpConsumeResult.appendNode(block: (items: List<Any>) -> NodeData): KSharpConsumeResult =
    map {
        val items = it.collection.build()
        val item = block(items)
        NodeCollector(listBuilder<Any>().apply { add(item as Any) }, it.tokens)
    }

fun <R> KSharpConsumeResult.enableLabelToken(code: (KSharpConsumeResult) -> Either<ParserError<KSharpLexerState>, R>): Either<ParserError<KSharpLexerState>, R> =
    flatMap { collector ->
        val result = this@enableLabelToken
        collector.tokens.enableLabelToken {
            code(result)
        }
    }

fun <R> KSharpConsumeResult.enableIfKeywords(code: (KSharpConsumeResult) -> Either<ParserError<KSharpLexerState>, R>): Either<ParserError<KSharpLexerState>, R> =
    flatMap { collector ->
        val result = this@enableIfKeywords
        collector.tokens.enableMapElseThenKeywords {
            code(result)
        }
    }

fun <R> KSharpConsumeResult.enableLetKeywords(code: (KSharpConsumeResult) -> Either<ParserError<KSharpLexerState>, R>): Either<ParserError<KSharpLexerState>, R> =
    flatMap { collector ->
        val result = this@enableLetKeywords
        collector.tokens.enableMapThenKeywords {
            code(result)
        }
    }

fun <R> KSharpConsumeResult.enableDiscardBlockAndNewLineTokens(code: (KSharpConsumeResult) -> Either<ParserError<KSharpLexerState>, R>): Either<ParserError<KSharpLexerState>, R> =
    flatMap { collector ->
        val result = this@enableDiscardBlockAndNewLineTokens
        collector.tokens.enableDiscardBlockAndNewLineTokens {
            code(result)
        }
    }

fun <R> KSharpConsumeResult.disableExpressionStartingNewLine(code: (KSharpConsumeResult) -> Either<ParserError<KSharpLexerState>, R>): Either<ParserError<KSharpLexerState>, R> =
    flatMap { collector ->
        val result = this@disableExpressionStartingNewLine
        collector.tokens.disableExpressionStartingNewLine {
            code(result)
        }
    }

fun KSharpLexerIterator.consumeBlock(action: (KSharpLexerIterator) -> KSharpParserResult): KSharpParserResult =
    consume(KSharpTokenType.BeginBlock, true).flatMap { collector ->
        action(collector.tokens).endBlock()
    }

fun KSharpConsumeResult.thenInBlock(action: (KSharpLexerIterator) -> KSharpParserResult): KSharpConsumeResult =
    consume {
        it.consumeBlock { block ->
            action(block)
        }
    }


fun KSharpLexerIterator.consumeDot() = consume(KSharpTokenType.Operator0, ".")

fun KSharpLexerIterator.consumeLowerCaseWord(text: String? = null, discardToken: Boolean = false) =
    if (text != null) {
        consume(KSharpTokenType.LowerCaseWord, text, discardToken)
    } else consume(KSharpTokenType.LowerCaseWord, discardToken)

fun KSharpConsumeResult.thenLowerCaseWord(text: String? = null, discardToken: Boolean = false) =
    if (text != null) {
        then(KSharpTokenType.LowerCaseWord, text, discardToken)
    } else then(KSharpTokenType.LowerCaseWord, discardToken)

fun KSharpConsumeResult.thenUpperCaseWord(discardToken: Boolean = false) =
    then(KSharpTokenType.UpperCaseWord, discardToken)

fun KSharpLexerIterator.consumeKeyword(text: String, discardToken: Boolean = false) =
    consumeLowerCaseWord(text, discardToken)

fun KSharpConsumeResult.thenKeyword(text: String, discardToken: Boolean = false) =
    thenLowerCaseWord(text, discardToken)

fun KSharpConsumeResult.thenNewLine() =
    then(KSharpTokenType.NewLine, true)

private fun KSharpParserResult.endBlock(): KSharpParserResult =
    when (this) {
        is Either.Left -> {
            if (value.consumedTokens) {
                val iter = value.remainTokens
                var result = Either.Left(ParserError(value.error, true, emptyLexerIterator(value.remainTokens.state)))
                while (iter.hasNext()) {
                    val tk = iter.next()
                    if (tk.type == KSharpTokenType.EndBlock) {
                        result = Either.Left(ParserError(value.error, true, iter))
                        break
                    }
                }
                result
            } else this
        }

        is Either.Right -> {
            value.remainTokens.optionalConsume(KSharpTokenType.NewLine)
                .then(KSharpTokenType.EndBlock, false)
                .map {
                    ParserValue(value.value, it.tokens)
                }
        }
    }

fun KSharpConsumeResult.thenAssignOperator() =
    then(KSharpTokenType.AssignOperator, true)

fun Reader.parseModule(
    context: String,
    withLocations: Boolean = false
): ParserErrorOrValue<KSharpLexerState, ModuleNode> =
    kSharpLexer()
        .collapseKSharpTokens()
        .cast<TokenLexerIterator<KSharpLexerState>>()
        .let {
            if (withLocations) it.toLogicalLexerToken(context, KSharpTokenType.NewLine)
            else it
        }.markBlocks {
            val token = LexerToken(it, TextToken("", 0, 0))
            if (withLocations) LogicalLexerToken(token, context, ZeroPosition, ZeroPosition)
            else token
        }.consumeModule(context)
        .map { it.value }

fun Path.parseModule(withLocations: Boolean = false) =
    Files.newBufferedReader(this, StandardCharsets.UTF_8).parseModule(fileName.toString(), withLocations)

fun File.parseModule(withLocations: Boolean = false) =
    reader(StandardCharsets.UTF_8).parseModule(name, withLocations)

fun String.parseModule(context: String, withLocations: Boolean = false) = reader().parseModule(context, withLocations)
