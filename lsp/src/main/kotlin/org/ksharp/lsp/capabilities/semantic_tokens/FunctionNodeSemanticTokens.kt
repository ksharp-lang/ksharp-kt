package org.ksharp.lsp.capabilities.semantic_tokens

import org.eclipse.lsp4j.SemanticTokenTypes
import org.ksharp.nodes.FunctionNode

fun FunctionNode.semanticTokens(encoder: TokenEncoder) {
    annotations?.forEach {
        it.visit(encoder)
    }
    if (native) {
        encoder.register(locations.nativeLocation, SemanticTokenTypes.Keyword)
    }
    if (pub)
        encoder.register(locations.pubLocation, SemanticTokenTypes.Keyword)
    encoder.register(locations.name, SemanticTokenTypes.Method)
    locations.parameters.forEach {
        encoder.register(it, SemanticTokenTypes.Parameter)
    }
    if (!native) {
        encoder.register(locations.assignOperator, SemanticTokenTypes.Operator)
    }
    expression.visit(encoder)
}
