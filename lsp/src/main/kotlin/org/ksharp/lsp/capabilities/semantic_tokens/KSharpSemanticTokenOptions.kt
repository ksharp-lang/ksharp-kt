package org.ksharp.lsp.capabilities.semantic_tokens

import org.eclipse.lsp4j.SemanticTokenTypes
import org.eclipse.lsp4j.SemanticTokensLegend
import org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions

val kSharpSemanticTokensProvider = SemanticTokensWithRegistrationOptions().apply {
    legend = SemanticTokensLegend().apply {
        tokenTypes = listOf(
            SemanticTokenTypes.Type,
            SemanticTokenTypes.String,
            SemanticTokenTypes.Function,
            SemanticTokenTypes.Variable
        )
        tokenModifiers = listOf()
    }
    setFull(true)
    setRange(false)

}
