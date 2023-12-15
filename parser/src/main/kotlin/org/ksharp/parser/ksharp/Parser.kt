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

private val TypeRegexp = Regex("[a-z][a-zA-Z0-9_]*\\.[a-zA-Z0-9_]+")

fun String.isValidType(): Boolean =
    TypeRegexp.matches(this)

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

fun KSharpConsumeResult.lastNodeData(): KSharpParserResult =
    build { it.last().cast<NodeData>() }

fun KSharpLexerIterator.consumeDot() = consume(KSharpTokenType.Operator0, ".")

fun KSharpLexerIterator.consumeLowerCaseWord(text: String? = null) =
    if (text != null) {
        consume(KSharpTokenType.LowerCaseWord, text, false)
    } else consume(KSharpTokenType.LowerCaseWord, false)

fun KSharpConsumeResult.thenTypeName() =
    then({
        when {
            it.type == KSharpTokenType.UpperCaseWord -> true
            it.type == KSharpTokenType.FunctionName && it.text.isValidType() -> true
            else -> false
        }
    }, {
        createExpectedTokenError("<Type>", it)
    }, false)

fun KSharpConsumeResult.thenLowerCaseWord(text: String? = null) =
    if (text != null) {
        then(KSharpTokenType.LowerCaseWord, text, false)
    } else then(KSharpTokenType.LowerCaseWord, false)

fun KSharpConsumeResult.thenUpperCaseWord() =
    then(KSharpTokenType.UpperCaseWord, false)

fun KSharpLexerIterator.consumeKeyword(text: String) =
    consumeLowerCaseWord(text)

fun KSharpConsumeResult.thenKeyword(text: String) =
    thenLowerCaseWord(text)

fun KSharpConsumeResult.thenAssignOperator() =
    then(KSharpTokenType.AssignOperator, false)


fun String.lexerModule(withLocations: Boolean) =
    this.reader().lexerModule(withLocations)

fun Reader.lexerModule(withLocations: Boolean) =
    kSharpLexer()
        .filterAndCollapseTokens()
        .cast<TokenLexerIterator<KSharpLexerState>>()
        .let {
            if (withLocations) it.toLogicalLexerToken()
            else it
        }.excludeIgnoreNewLineTokens()
        .collapseNewLines()
        .enableLookAhead()
        .enableIndentationOffset()


fun Reader.parseModule(
    name: String,
    withLocations: Boolean
): ParserErrorOrValue<KSharpLexerState, ModuleNode> =
    lexerModule(withLocations)
        .emitLocations(withLocations) {
            it.consumeModule(name)
        }
        .map { it.value }

fun Path.parseModule(withLocations: Boolean) =
    Files.newBufferedReader(this, StandardCharsets.UTF_8).parseModule(fileName.toString(), withLocations)

fun File.parseModule(withLocations: Boolean) =
    reader(StandardCharsets.UTF_8).parseModule(name, withLocations)

fun String.parseModule(name: String, withLocations: Boolean) = reader().parseModule(name, withLocations)

fun String.parseModuleAsNodeSequence(): List<NodeData> =
    lexerModule(true)
        .emitLocations(true) {
            it.consumeModuleNodes()
        }
