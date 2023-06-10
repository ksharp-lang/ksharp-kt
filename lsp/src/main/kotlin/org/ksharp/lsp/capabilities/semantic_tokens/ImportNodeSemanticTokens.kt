package org.ksharp.lsp.capabilities.semantic_tokens

import org.eclipse.lsp4j.SemanticTokenTypes
import org.ksharp.common.Location
import org.ksharp.nodes.ImportNode

fun ImportNode.semanticTokens(encoder: TokenEncoder) {
    encoder.register(locations.importLocation, SemanticTokenTypes.Keyword)
    encoder.register(
        Location(locations.moduleNameBegin.start, locations.moduleNameEnd.end),
        SemanticTokenTypes.Namespace
    )
    encoder.register(locations.asLocation, SemanticTokenTypes.Keyword)
    encoder.register(locations.keyLocation, SemanticTokenTypes.Namespace)
}
