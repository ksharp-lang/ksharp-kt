package org.ksharp.parser.ksharp

import org.ksharp.common.ErrorOrValue
import org.ksharp.common.ZeroPosition
import org.ksharp.nodes.ModuleNode
import org.ksharp.parser.*
import java.io.File
import java.io.Reader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

fun <L : LexerValue> Iterator<L>.consumeDot() = consume(KSharpTokenType.Operator, ".")
fun <L : LexerValue> Iterator<L>.consumeLowerCaseWord(text: String? = null) = if (text != null) {
    consume(KSharpTokenType.LowerCaseWord, text)
} else consume(KSharpTokenType.LowerCaseWord)

fun <L : LexerValue> WithNodeCollector<L>.thenLowerCaseWord(text: String? = null) = if (text != null) {
    then(KSharpTokenType.LowerCaseWord, text)
} else then(KSharpTokenType.LowerCaseWord)

fun <L : LexerValue> Iterator<L>.consumeKeyword(text: String) = consumeLowerCaseWord(text)
fun <L : LexerValue> WithNodeCollector<L>.thenKeyword(text: String) = thenLowerCaseWord(text)


fun Reader.parseModule(context: String, withLocations: Boolean = false): ErrorOrValue<ModuleNode> =
    kSharpLexer()
        .collapseKSharpTokens()
        .let {
            if (withLocations) it.toLogicalLexerToken(context, KSharpTokenType.NewLine)
            else it
        }
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
