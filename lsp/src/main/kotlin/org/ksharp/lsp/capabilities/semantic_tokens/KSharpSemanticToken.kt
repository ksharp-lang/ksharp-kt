package org.ksharp.lsp.capabilities.semantic_tokens

import org.eclipse.lsp4j.SemanticTokenModifiers
import org.eclipse.lsp4j.SemanticTokenTypes
import org.eclipse.lsp4j.SemanticTokensLegend
import org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions
import org.ksharp.common.Either
import org.ksharp.common.ErrorOrValue
import org.ksharp.common.cast
import org.ksharp.parser.LogicalLexerToken
import org.ksharp.parser.Token
import org.ksharp.parser.asSequence
import org.ksharp.parser.ksharp.KSharpTokenType
import org.ksharp.parser.ksharp.lexerModule


val tokenEncoderSpec = tokenEncoderSpec {
    tokens {
        +SemanticTokenTypes.Type
        +SemanticTokenTypes.String
        +SemanticTokenTypes.Function
        +SemanticTokenTypes.Variable
        +SemanticTokenTypes.Operator
        +SemanticTokenTypes.Number
        +SemanticTokenTypes.Keyword
    }
    modifiers {
        +SemanticTokenModifiers.Declaration
    }
}

val kSharpSemanticTokensProvider = SemanticTokensWithRegistrationOptions().apply {
    legend = SemanticTokensLegend().apply {
        tokenTypes = tokenEncoderSpec.tokens
        tokenModifiers = tokenEncoderSpec.modifiers
    }
    setFull(true)
    setRange(false)
}

fun Token.semanticToken(): String? =
    when (type) {
        KSharpTokenType.String -> SemanticTokenTypes.String
        KSharpTokenType.FunctionName -> SemanticTokenTypes.Function
        KSharpTokenType.OperatorFunctionName -> SemanticTokenTypes.Function
        KSharpTokenType.UpperCaseWord -> SemanticTokenTypes.Type
        KSharpTokenType.Operator -> SemanticTokenTypes.Operator
        KSharpTokenType.Operator0 -> SemanticTokenTypes.Operator
        KSharpTokenType.Operator1 -> SemanticTokenTypes.Operator
        KSharpTokenType.Operator2 -> SemanticTokenTypes.Operator
        KSharpTokenType.Operator3 -> SemanticTokenTypes.Operator
        KSharpTokenType.Operator4 -> SemanticTokenTypes.Operator
        KSharpTokenType.Operator5 -> SemanticTokenTypes.Operator
        KSharpTokenType.Operator6 -> SemanticTokenTypes.Operator
        KSharpTokenType.Operator7 -> SemanticTokenTypes.Operator
        KSharpTokenType.Operator8 -> SemanticTokenTypes.Operator
        KSharpTokenType.Operator9 -> SemanticTokenTypes.Operator
        KSharpTokenType.Operator10 -> SemanticTokenTypes.Operator
        KSharpTokenType.Operator11 -> SemanticTokenTypes.Operator
        KSharpTokenType.Operator12 -> SemanticTokenTypes.Operator
        KSharpTokenType.HexInteger -> SemanticTokenTypes.Number
        KSharpTokenType.OctalInteger -> SemanticTokenTypes.Number
        KSharpTokenType.BinaryInteger -> SemanticTokenTypes.Number
        KSharpTokenType.Integer -> SemanticTokenTypes.Number
        KSharpTokenType.Float -> SemanticTokenTypes.Number
        KSharpTokenType.LowerCaseWord -> when (text) {
            "type" -> SemanticTokenTypes.Keyword
            "native" -> SemanticTokenTypes.Keyword
            "pub" -> SemanticTokenTypes.Keyword
            else -> null
        }

        KSharpTokenType.If -> SemanticTokenTypes.Keyword
        KSharpTokenType.Then -> SemanticTokenTypes.Keyword
        KSharpTokenType.Else -> SemanticTokenTypes.Keyword
        KSharpTokenType.Let -> SemanticTokenTypes.Keyword
        else -> null
    }

fun Token.semanticTokenModifiers(): Array<String> =
    when (type) {
        else -> emptyArray<String>()
    }

fun calculateSemanticTokens(content: String): ErrorOrValue<List<Int>> =
    tokenEncoderSpec.encoder()
        .let { encoder ->
            content.lexerModule(withLocations = true)
                .asSequence()
                .forEach {
                    it.semanticToken()?.let { tokenType ->
                        val tk = it.cast<LogicalLexerToken>()
                        val (line, offset) = tk.startPosition
                        encoder.register(
                            line.value,
                            offset.value,
                            tk.endOffset - tk.startOffset,
                            tokenType,
                            *tk.semanticTokenModifiers()
                        )
                    }
                }
            Either.Right(encoder.data())
        }
