package org.ksharp.lsp.capabilities.semantic_tokens

import org.ksharp.parser.ksharp.InvalidNode

fun InvalidNode.semanticTokens(encoder: TokenEncoder) {
    this.tokens.forEach {
        it.type.semanticToken(it.text)?.let { tokenType ->
            encoder.register(it.location, tokenType)
        }
    }
}
