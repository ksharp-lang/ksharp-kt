package org.ksharp.lsp.capabilities.semantic_tokens

import org.eclipse.lsp4j.SemanticTokenModifiers
import org.eclipse.lsp4j.SemanticTokenTypes
import org.ksharp.common.cast
import org.ksharp.nodes.ImplNode
import org.ksharp.nodes.NodeData

fun ImplNode.semanticTokens(encoder: TokenEncoder) {
    encoder.register(location, SemanticTokenTypes.Keyword)
    encoder.register(locations.traitName, SemanticTokenTypes.Type, SemanticTokenModifiers.Declaration)
    encoder.register(locations.forKeyword, SemanticTokenTypes.Keyword)
    forType.cast<NodeData>().visit(encoder)
    encoder.register(locations.assignOperator, SemanticTokenTypes.Operator)
    functions.forEach {
        it.visit(encoder)
    }
}
