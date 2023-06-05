package org.ksharp.lsp.capabilities.semantic_tokens

import org.eclipse.lsp4j.SemanticTokenTypes
import org.eclipse.lsp4j.SemanticTokensLegend
import org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions
import org.ksharp.common.Either
import org.ksharp.common.Error
import org.ksharp.common.ErrorOrValue
import org.ksharp.parser.ksharp.parseModule

val kSharpSemanticTokensProvider = SemanticTokensWithRegistrationOptions().apply {
    legend = SemanticTokensLegend().apply {
        tokenTypes = listOf(
            SemanticTokenTypes.Type,
            SemanticTokenTypes.String,
            SemanticTokenTypes.Function,
            SemanticTokenTypes.Variable,
        )
        tokenModifiers = listOf()
    }
    setFull(true)
    setRange(false)
}


fun calculateSemanticTokens(uri: String, content: String): ErrorOrValue<List<Int>> =
    content.parseModule(uri, withLocations = true)
        .flatMap<Error, List<Int>> {
            Either.Right(listOf<Int>())
        }.flatMapLeft {
            Either.Right(listOf<Int>())
        }
