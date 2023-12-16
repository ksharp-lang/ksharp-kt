package org.ksharp.lsp.actions

import org.ksharp.lsp.capabilities.semantic_tokens.tokenEncoderSpec
import org.ksharp.lsp.capabilities.semantic_tokens.visit
import org.ksharp.nodes.NodeData

val SemanticTokensAction = ActionId<List<Int>>("SemanticTokenAction")
fun ActionCatalog.semanticTokensAction() = action<List<NodeData>, List<Int>>(
    SemanticTokensAction,
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
