package org.ksharp.lsp.capabilities.semantic_tokens

import org.ksharp.nodes.*
import org.ksharp.parser.ksharp.InvalidNode

fun NodeData.visit(encoder: TokenEncoder) {
    when (this) {
        is InvalidNode -> semanticTokens(encoder)
        is ImportNode -> semanticTokens(encoder)
        is AnnotationNode -> semanticTokens(encoder)
        is TypeDeclarationNode -> semanticTokens(encoder)
        is TypeNode -> semanticTokens(encoder)
        is ConcreteTypeNode -> semanticTokens(encoder)
        is ParameterTypeNode -> semanticTokens(encoder)
        is ParametricTypeNode -> semanticTokens(encoder)
        is LabelTypeNode -> semanticTokens(encoder)
        is FunctionTypeNode -> semanticTokens(encoder)
        is TupleTypeNode -> semanticTokens(encoder)
        is UnionTypeNode -> semanticTokens(encoder)
        is IntersectionTypeNode -> semanticTokens(encoder)
        is ConstrainedTypeNode -> semanticTokens(encoder)
        is TraitNode -> semanticTokens(encoder)
        is TraitFunctionNode -> semanticTokens(encoder)
        is TraitFunctionsNode -> semanticTokens(encoder)
        is ImplNode -> semanticTokens(encoder)

        is FunctionNode -> semanticTokens(encoder)

        is OperatorNode -> semanticTokens(encoder)
        is LiteralCollectionNode -> semanticTokens(encoder)
        is LiteralValueNode -> semanticTokens(encoder)
        is LiteralMapEntryNode -> semanticTokens(encoder)
        is FunctionCallNode -> semanticTokens(encoder)
        is IfNode -> semanticTokens(encoder)
        is LetExpressionNode -> semanticTokens(encoder)
        is MatchListValueNode -> semanticTokens(encoder)
        is MatchAssignNode -> semanticTokens(encoder)
        else -> Unit
    }
}
