package org.ksharp.lsp.capabilities.semantic_tokens

import org.ksharp.common.cast
import org.ksharp.parser.LogicalLexerToken
import org.ksharp.parser.ksharp.InvalidNode

fun InvalidNode.semanticTokens(encoder: TokenEncoder) =
    this.tokens.forEach {
        it.type.semanticToken(it.text)?.let { tokenType ->
            val tk = it.cast<LogicalLexerToken>()
            val (line, offset) = tk.startPosition
            encoder.register(
                line.value,
                offset.value,
                tk.endOffset - tk.startOffset,
                tokenType
            )
        }
    }
