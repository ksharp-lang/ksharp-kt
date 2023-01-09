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
    val function: TraitFunctionsNode,
    override val location: Location
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(function)

}

data class LabelTypeNode(
    val name: String,
    val expr: TypeExpression,
    override val location: Location
) : NodeData(), TypeExpression {
    override val children: Sequence<NodeData>
        get() = sequenceOf(expr.cast())
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