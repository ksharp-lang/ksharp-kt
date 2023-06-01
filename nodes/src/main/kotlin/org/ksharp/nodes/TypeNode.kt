package org.ksharp.nodes

import org.ksharp.common.Location
import org.ksharp.common.cast

interface TypeExpression {
    val representation: String
}

data class TraitFunctionNode(
    val name: String,
    val type: NodeData,
    override val location: Location
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(type)

}

data class TraitFunctionsNode(
    val functions: List<TraitFunctionNode>
) : NodeData() {
    override val location: Location
        get() = Location.NoProvided
    override val children: Sequence<NodeData>
        get() = functions.asSequence()
}

data class TraitNode(
    val internal: Boolean,
    val annotations: List<AnnotationNode>?,
    val name: String,
    val params: List<String>,
    val definition: TraitFunctionsNode,
    override val location: Location
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(definition)

}

data class LabelTypeNode(
    val name: String,
    val expr: TypeExpression,
    override val location: Location
) : NodeData(), TypeExpression {
    override val children: Sequence<NodeData>
        get() = sequenceOf(expr.cast())

    override val representation: String
        get() = "$name = ${expr.representation}"
}

data class UnitTypeNode(
    override val location: Location
) : NodeData(), TypeExpression {
    override val children: Sequence<NodeData>
        get() = emptySequence()

    override val representation: String
        get() = "()"
}

data class ConcreteTypeNode(
    val name: String,
    override val location: Location
) : NodeData(), TypeExpression {
    override val children: Sequence<NodeData>
        get() = emptySequence()

    override val representation: String
        get() = name
}

data class ParameterTypeNode(
    val name: String,
    override val location: Location
) : NodeData(), TypeExpression {
    override val children: Sequence<NodeData>
        get() = emptySequence()

    override val representation: String
        get() = name
}

data class ParametricTypeNode(
    val variables: List<TypeExpression>,
    override val location: Location
) : NodeData(), TypeExpression {
    override val children: Sequence<NodeData>
        get() = variables.asSequence().cast()

    override val representation: String
        get() = "(${variables.joinToString(" ") { it.representation }})"
}

data class FunctionTypeNode(
    val params: List<TypeExpression>,
    override val location: Location
) : NodeData(), TypeExpression {
    override val children: Sequence<NodeData>
        get() = params.asSequence().cast()

    override val representation: String
        get() = "(${params.joinToString(" -> ") { it.representation }})"
}

data class TupleTypeNode(
    val types: List<TypeExpression>,
    override val location: Location
) : NodeData(), TypeExpression {
    override val children: Sequence<NodeData>
        get() = types.asSequence().cast()

    override val representation: String
        get() = "(${types.joinToString(" , ") { it.representation }})"
}

data class ConstrainedTypeNode(
    val type: TypeExpression,
    val expression: NodeData,
    override val location: Location
) : NodeData(), TypeExpression {
    override val children: Sequence<NodeData>
        get() = sequenceOf(type, expression).cast()

    override val representation: String
        get() = type.representation
}

data class InvalidSetTypeNode(
    override val location: Location
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = emptySequence()
}

data class SetElement(
    val union: Boolean,
    val expression: TypeExpression,
    override val location: Location
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = emptySequence()

}

data class UnionTypeNode(
    val types: List<TypeExpression>,
    override val location: Location
) : NodeData(), TypeExpression {
    override val children: Sequence<NodeData>
        get() = types.asSequence().cast()

    override val representation: String
        get() = "(${types.joinToString(" | ") { it.representation }})"
}

data class IntersectionTypeNode(
    val types: List<TypeExpression>,
    override val location: Location
) : NodeData(), TypeExpression {
    override val children: Sequence<NodeData>
        get() = types.asSequence().cast()

    override val representation: String
        get() = "(${types.joinToString(" & ") { it.representation }})"

}

data class TypeNode(
    val internal: Boolean,
    val annotations: List<AnnotationNode>?,
    val name: String,
    val params: List<String>,
    val expr: NodeData,
    override val location: Location
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(expr)
}

data class TypeDeclarationNode(
    val annotations: List<AnnotationNode>?,
    val name: String,
    val params: List<String>,
    val type: TypeExpression,
    override val location: Location
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(type as NodeData)
}
