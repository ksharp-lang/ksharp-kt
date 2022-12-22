package org.ksharp.parser.ksharp

import org.ksharp.common.ZeroPosition
import org.ksharp.common.cast
import org.ksharp.nodes.ModuleNode
import org.ksharp.nodes.NodeData
import org.ksharp.parser.*
import java.io.File
import java.io.Reader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

typealias KSharpParserResult<L> = ParserResult<NodeData, L>


fun <L : LexerValue> Iterator<L>.consumeDot() = consume(KSharpTokenType.Operator, ".")
fun <L : LexerValue> Iterator<L>.consumeLowerCaseWord(text: String? = null, discardToken: Boolean = false) =
    if (text != null) {
        consume(KSharpTokenType.LowerCaseWord, text, discardToken)
    } else consume(KSharpTokenType.LowerCaseWord, discardToken)

fun <L : LexerValue> ConsumeResult<L>.thenLowerCaseWord(text: String? = null, discardToken: Boolean = false) =
    if (text != null) {
        then(KSharpTokenType.LowerCaseWord, text, discardToken)
    } else then(KSharpTokenType.LowerCaseWord, discardToken)

fun <L : LexerValue> ConsumeResult<L>.thenUpperCaseWord(discardToken: Boolean = false) =
    then(KSharpTokenType.UpperCaseWord, discardToken)

fun <L : LexerValue> Iterator<L>.consumeKeyword(text: String, discardToken: Boolean = false) =
    consumeLowerCaseWord(text, discardToken)

fun <L : LexerValue> ConsumeResult<L>.thenKeyword(text: String, discardToken: Boolean = false) =
    thenLowerCaseWord(text, discardToken)

fun <L : LexerValue> ConsumeResult<L>.thenEndExpression() = then(KSharpTokenType.EndExpression, true)

fun <L : LexerValue> ConsumeResult<L>.thenAssignOperator() =
    then(KSharpTokenType.Operator12, true)

fun Reader.parseModule(
    context: String,
    withLocations: Boolean = false
): ParserErrorOrValue<CollapsableToken, ModuleNode> =
    kSharpLexer()
        .collapseKSharpTokens()
        .let {
            if (withLocations) it.toLogicalLexerToken(context, KSharpTokenType.NewLine)
            else it
        }.cast<Iterator<CollapsableToken>>()
        .markExpressions {
            val token = LexerToken(KSharpTokenType.EndExpression, TextToken("$it", 0, 0))
            if (withLocations) LogicalLexerToken(token, context, ZeroPosition, ZeroPosition)
            else token
        }
        .consumeModule(context)
        .map { it.value }

fun Path.parseModule(withLocations: Boolean = false) =
    Files.newBufferedReader(this, StandardCharsets.UTF_8).parseModule(fileName.toString(), withLocations)

fun File.parseModule(withLocations: Boolean = false) =
    reader(StandardCharsets.UTF_8).parseModule(name, withLocations)

fun String.parseModule(context: String, withLocations: Boolean = false) = reader().parseModule(context, withLocations)
