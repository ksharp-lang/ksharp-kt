package org.ksharp.lsp.capabilities.semantic_tokens

import org.eclipse.lsp4j.SemanticTokenModifiers
import org.eclipse.lsp4j.SemanticTokenTypes
import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.nodes.*

fun TypeDeclarationNode.semanticTokens(encoder: TokenEncoder) {
    annotations?.forEach {
        it.visit(encoder)
    }
    encoder.register(locations.name, SemanticTokenTypes.Method)
    encoder.register(locations.separator, SemanticTokenTypes.Operator)
    locations.params.forEach {
        encoder.register(it, SemanticTokenTypes.Parameter)
    }
    type.cast<NodeData>().visit(encoder)
}

fun TypeNode.semanticTokens(encoder: TokenEncoder) {
    annotations?.forEach {
        it.visit(encoder)
    }
    if (internal)
        encoder.register(locations.internalLocation, SemanticTokenTypes.Keyword)
    encoder.register(locations.typeLocation, SemanticTokenTypes.Keyword)
    encoder.register(locations.name, SemanticTokenTypes.Type, SemanticTokenModifiers.Declaration)
    locations.params.forEach {
        encoder.register(it, SemanticTokenTypes.TypeParameter)
    }
    encoder.register(locations.assignOperatorLocation, SemanticTokenTypes.Operator)
    expr.visit(encoder)
}

fun ConcreteTypeNode.semanticTokens(encoder: TokenEncoder) {
    encoder.register(location, SemanticTokenTypes.Variable)
}

fun ParameterTypeNode.semanticTokens(encoder: TokenEncoder) {
    encoder.register(location, SemanticTokenTypes.Parameter)
}

fun ParametricTypeNode.semanticTokens(encoder: TokenEncoder) {
    variables.forEach {
        it.cast<NodeData>().visit(encoder)
    }
}

fun LabelTypeNode.semanticTokens(encoder: TokenEncoder) {
    encoder.register(location, SemanticTokenTypes.Comment)
    expr.cast<NodeData>().visit(encoder)
}

private fun TokenEncoder.registerWithSeparators(nodes: List<NodeData>, separators: List<Location>) {
    val nodesIter = nodes.iterator()
    val separatorsIter = separators.iterator()
    if (nodesIter.hasNext()) {
        nodesIter.next().visit(this)
    }
    while (nodesIter.hasNext() && separatorsIter.hasNext()) {
        val separator = separatorsIter.next()
        register(separator, SemanticTokenTypes.Operator)
        nodesIter.next().visit(this)
    }
}

fun FunctionTypeNode.semanticTokens(encoder: TokenEncoder) {
    encoder.registerWithSeparators(params.cast(), locations.separators)
}

fun TupleTypeNode.semanticTokens(encoder: TokenEncoder) {
    encoder.registerWithSeparators(types.cast(), locations.separators)
}

fun UnionTypeNode.semanticTokens(encoder: TokenEncoder) {
    encoder.registerWithSeparators(types.cast(), locations.separators)
}

fun IntersectionTypeNode.semanticTokens(encoder: TokenEncoder) {
    encoder.registerWithSeparators(types.cast(), locations.separators)
}

fun ConstrainedTypeNode.semanticTokens(encoder: TokenEncoder) {
    type.cast<NodeData>().visit(encoder)
    encoder.register(locations.separator, SemanticTokenTypes.Operator)
    expression.visit(encoder)
}

fun TraitNode.semanticTokens(encoder: TokenEncoder) {
    annotations?.forEach {
        it.visit(encoder)
    }
    if (internal)
        encoder.register(locations.internalLocation, SemanticTokenTypes.Keyword)
    encoder.register(locations.traitLocation, SemanticTokenTypes.Keyword)
    encoder.register(locations.name, SemanticTokenTypes.Type, SemanticTokenModifiers.Declaration)
    locations.params.forEach {
        encoder.register(it, SemanticTokenTypes.TypeParameter)
    }
    encoder.register(locations.assignOperatorLocation, SemanticTokenTypes.Operator)
    definition.visit(encoder)
}

fun TraitFunctionsNode.semanticTokens(encoder: TokenEncoder) {
    definitions.forEach { it.visit(encoder) }
}

fun TraitFunctionNode.semanticTokens(encoder: TokenEncoder) {
    encoder.register(locations.name, SemanticTokenTypes.Method)
    encoder.register(locations.operator, SemanticTokenTypes.Operator)
    type.visit(encoder)
}
