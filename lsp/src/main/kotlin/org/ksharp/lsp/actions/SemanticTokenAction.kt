package org.ksharp.lsp.actions

import org.ksharp.lsp.capabilities.semantic_tokens.tokenEncoderSpec
import org.ksharp.lsp.capabilities.semantic_tokens.visit
import org.ksharp.nodes.NodeData

const val SemanticTokenAction = "SemanticTokenAction"
fun semanticTokenAction() = action<List<NodeData>, List<Int>>(
    SemanticTokenAction,
    listOf()
) {
    execution { _, nodes ->
        tokenEncoderSpec.encoder()
            .let { encoder ->
                nodes.forEach {
                    it.visit(encoder)
                }
                encoder.data()
            }
    }
}
