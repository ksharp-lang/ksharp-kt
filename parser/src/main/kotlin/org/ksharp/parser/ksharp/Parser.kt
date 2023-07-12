package org.ksharp.parser.ksharp

import org.ksharp.common.Either
import org.ksharp.common.add
import org.ksharp.common.cast
import org.ksharp.common.listBuilder
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

fun <R> KSharpConsumeResult.disableCollapseAssignOperatorRule(code: (KSharpConsumeResult) -> Either<ParserError<KSharpLexerState>, R>): Either<ParserError<KSharpLexerState>, R> =
    flatMap { collector ->
        val result = this@disableCollapseAssignOperatorRule
        collector.tokens.disableCollapseAssignOperatorRule {
            code(result)
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

fun KSharpConsumeResult.thenAssignOperator() =
    then(KSharpTokenType.AssignOperator, false)

fun KSharpLexerIterator.ifStartRepeatingLine(action: (KSharpConsumeResult) -> KSharpParserResult) =
    ifConsume({
        it.type == BaseTokenType.NewLine && state.value.indentationOffset.currentType == OffsetType.Repeating
    }, true, action)

fun String.lexerModule(withLocations: Boolean) =
    this.reader().lexerModule(withLocations)

fun Reader.lexerModule(withLocations: Boolean) =
    kSharpLexer()
        .filterAndCollapseTokens()
        .cast<TokenLexerIterator<KSharpLexerState>>()
        .let {
            if (withLocations) it.toLogicalLexerToken()
            else it
        }.collapseNewLines()
        .enableLookAhead()
        .enableIndentationOffset()


fun Reader.parseModule(
    name: String,
    withLocations: Boolean = false
): ParserErrorOrValue<KSharpLexerState, ModuleNode> =
    lexerModule(withLocations)
        .emitLocations(withLocations) {
            it.consumeModule(name)
        }
        .map { it.value }

fun Path.parseModule(withLocations: Boolean = false) =
    Files.newBufferedReader(this, StandardCharsets.UTF_8).parseModule(fileName.toString(), withLocations)

fun File.parseModule(withLocations: Boolean = false) =
    reader(StandardCharsets.UTF_8).parseModule(name, withLocations)

fun String.parseModule(name: String, withLocations: Boolean = false) = reader().parseModule(name, withLocations)

fun String.parseModuleAsNodeSequence(): List<NodeData> =
    lexerModule(true)
        .emitLocations(true) {
            it.consumeModuleNodes()
        }
