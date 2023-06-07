package org.ksharp.lsp.capabilities.semantic_tokens

import org.ksharp.nodes.NodeData
import org.ksharp.parser.ksharp.InvalidNode

fun NodeData.visit(encoder: TokenEncoder) =
    when (this) {
        is InvalidNode -> this.semanticTokens(encoder)
        else -> {}
    }
