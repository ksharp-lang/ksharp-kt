package org.ksharp.lsp.capabilities.semantic_tokens

import org.eclipse.lsp4j.SemanticTokenModifiers
import org.eclipse.lsp4j.SemanticTokenTypes
import org.eclipse.lsp4j.SemanticTokensLegend
import org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions
import org.ksharp.common.Location
import org.ksharp.parser.TokenType
import org.ksharp.parser.ksharp.KSharpTokenType
import org.ksharp.parser.ksharp.parseModuleAsNodeSequence


val tokenEncoderSpec = tokenEncoderSpec {
    tokens {
        +SemanticTokenTypes.Type
        +SemanticTokenTypes.String
        +SemanticTokenTypes.Function
        +SemanticTokenTypes.Variable
        +SemanticTokenTypes.Operator
        +SemanticTokenTypes.Number
        +SemanticTokenTypes.Keyword
        +SemanticTokenTypes.Namespace
        +SemanticTokenTypes.Method
        +SemanticTokenTypes.TypeParameter
        +SemanticTokenTypes.Parameter
        +SemanticTokenTypes.Comment
        +SemanticTokenTypes.Decorator
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

fun TokenType.semanticToken(text: String): String? =
    when (this) {
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
            "internal" -> SemanticTokenTypes.Keyword
            "let" -> SemanticTokenTypes.Keyword
            "if" -> SemanticTokenTypes.Keyword
            "then" -> SemanticTokenTypes.Keyword
            "as" -> SemanticTokenTypes.Keyword
            else -> null
        }

        KSharpTokenType.Alt -> SemanticTokenTypes.Operator
        KSharpTokenType.If -> SemanticTokenTypes.Keyword
        KSharpTokenType.Then -> SemanticTokenTypes.Keyword
        KSharpTokenType.Else -> SemanticTokenTypes.Keyword
        KSharpTokenType.Let -> SemanticTokenTypes.Keyword
        else -> null
    }


fun TokenEncoder.register(location: Location, tokenType: String, vararg modifiers: String) {
    val (line, offset) = location.start
    val (_, endOffset) = location.end
    register(
        line.value,
        offset.value,
        endOffset.value - offset.value,
        tokenType,
        *modifiers
    )
}

fun calculateSemanticTokens(content: String): List<Int> =
    tokenEncoderSpec.encoder()
        .let { encoder ->
            content.parseModuleAsNodeSequence()
                .forEach {
                    it.visit(encoder)
                }
            encoder.data()
        }
