package org.ksharp.nodes

import org.ksharp.common.Location
import org.ksharp.common.cast

interface TypeExpression

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
}

data class UnitTypeNode(
    override val location: Location
) : NodeData(), TypeExpression {
    override val children: Sequence<NodeData>
        get() = emptySequence()
}

data class ConcreteTypeNode(
    val name: String,
    override val location: Location
) : NodeData(), TypeExpression {
    override val children: Sequence<NodeData>
        get() = emptySequence()
}

data class ParameterTypeNode(
    val name: String,
    override val location: Location
) : NodeData(), TypeExpression {
    override val children: Sequence<NodeData>
        get() = emptySequence()
}

data class ParametricTypeNode(
    val variables: List<TypeExpression>,
    override val location: Location
) : NodeData(), TypeExpression {
    override val children: Sequence<NodeData>
        get() = variables.asSequence().cast()
}

data class FunctionTypeNode(
    val params: List<TypeExpression>,
    override val location: Location
) : NodeData(), TypeExpression {
    override val children: Sequence<NodeData>
        get() = params.asSequence().cast()
}

data class TupleTypeNode(
    val types: List<TypeExpression>,
    override val location: Location
) : NodeData(), TypeExpression {
    override val children: Sequence<NodeData>
        get() = types.asSequence().cast()
}

data class ConstrainedTypeNode(
    val type: TypeExpression,
    val expression: NodeData,
    override val location: Location
) : NodeData(), TypeExpression {
    override val children: Sequence<NodeData>
        get() = sequenceOf(type, expression).cast()
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

}

data class IntersectionTypeNode(
    val types: List<TypeExpression>,
    override val location: Location
) : NodeData(), TypeExpression {
    override val children: Sequence<NodeData>
        get() = types.asSequence().cast()

}

data class TypeNode(
    val internal: Boolean,
    val name: String,
    val params: List<String>,
    val expr: NodeData,
    override val location: Location
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(expr)
}

data class TypeDeclarationNode(
    val name: String,
    val params: List<String>,
    val type: TypeExpression,
    override val location: Location
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(type as NodeData)
}