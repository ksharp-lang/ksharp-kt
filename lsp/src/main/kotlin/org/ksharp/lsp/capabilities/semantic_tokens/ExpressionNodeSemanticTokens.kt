package org.ksharp.lsp.capabilities.semantic_tokens

import org.eclipse.lsp4j.SemanticTokenTypes
import org.ksharp.common.Location
import org.ksharp.nodes.*

fun OperatorNode.semanticTokens(encoder: TokenEncoder) {
    left.visit(encoder)
    encoder.register(location, SemanticTokenTypes.Operator)
    right.visit(encoder)
}

fun LiteralMapEntryNode.semanticTokens(encoder: TokenEncoder) {
    key.visit(encoder)
    encoder.register(locations.keyValueOperatorLocation, SemanticTokenTypes.Operator)
    value.visit(encoder)
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

        LiteralValueType.Label -> encoder.register(location, SemanticTokenTypes.Comment)

        else -> {}
    }
}

fun FunctionCallNode.semanticTokens(encoder: TokenEncoder) {
    if (arguments.isNotEmpty()) {
        encoder.register(location, SemanticTokenTypes.Function)
        arguments.forEach {
            it.visit(encoder)
        }
    }
}

fun IfNode.semanticTokens(encoder: TokenEncoder) {
    encoder.register(locations.ifLocation, SemanticTokenTypes.Operator)
    condition.visit(encoder)
    encoder.register(locations.thenLocation, SemanticTokenTypes.Operator)
    condition.visit(encoder)
    trueExpression.visit(encoder)
    if (locations.elseLocation != Location.NoProvided) {
        encoder.register(locations.elseLocation, SemanticTokenTypes.Operator)
        falseExpression.visit(encoder)
    }
}

fun LetExpressionNode.semanticTokens(encoder: TokenEncoder) {
    encoder.register(locations.letLocation, SemanticTokenTypes.Operator)
    matches.forEach { it.visit(encoder) }
    encoder.register(locations.thenLocation, SemanticTokenTypes.Operator)
    expression.visit(encoder)
}

fun MatchValueNode.semanticTokens(encoder: TokenEncoder) {
    value.visit(encoder)
}

fun MatchAssignNode.semanticTokens(encoder: TokenEncoder) {
    matchValue.visit(encoder)
    encoder.register(locations.assignOperatorLocation, SemanticTokenTypes.Operator)
    expression.visit(encoder)
}

fun MatchListValueNode.semanticTokens(encoder: TokenEncoder) {
    head.forEach {
        it.visit(encoder)
    }
    encoder.register(locations.tailSeparatorLocation, SemanticTokenTypes.Operator)
    tail.visit(encoder)
}
