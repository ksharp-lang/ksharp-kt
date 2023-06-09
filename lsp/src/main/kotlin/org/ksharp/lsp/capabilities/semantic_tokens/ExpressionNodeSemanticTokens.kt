package org.ksharp.lsp.capabilities.semantic_tokens

import org.eclipse.lsp4j.SemanticTokenTypes
import org.ksharp.nodes.LiteralCollectionNode
import org.ksharp.nodes.LiteralValueNode
import org.ksharp.nodes.LiteralValueType
import org.ksharp.nodes.OperatorNode

fun OperatorNode.semanticTokens(encoder: TokenEncoder) {
    left.visit(encoder)
    encoder.register(location, SemanticTokenTypes.Operator)
    right.visit(encoder)
}

fun LiteralCollectionNode.semanticTokens(encoder: TokenEncoder) {
    values.forEach {
        it.visit(encoder)
    }
}

fun LiteralValueNode.semanticTokens(encoder: TokenEncoder) {
    when (type) {
        LiteralValueType.Character, LiteralValueType.String, LiteralValueType.MultiLineString -> encoder.register(
            location,
            SemanticTokenTypes.String
        )

        LiteralValueType.BinaryInteger, LiteralValueType.Decimal, LiteralValueType.HexInteger, LiteralValueType.OctalInteger, LiteralValueType.Integer ->
            encoder.register(location, SemanticTokenTypes.Number)

        else -> {}
    }
}
