package org.ksharp.nodes

import org.ksharp.common.Location
import org.ksharp.common.cast

interface TypeExpression {
    val representation: String
}

data class TypeNodeLocations(
    val internalLocation: Location,
    val typeLocation: Location,
    val name: Location,
    val params: List<Location>,
    val assignOperatorLocation: Location
) : NodeLocations

data class TraitNodeLocations(
    val internalLocation: Location,
    val traitLocation: Location,
    val name: Location,
    val params: List<Location>,
    val assignOperatorLocation: Location
) : NodeLocations

data class TraitFunctionNodeLocation(
    val name: Location,
    val operator: Location
) : NodeLocations

data class FunctionTypeNodeLocations(
    val separators: List<Location>
) : NodeLocations

data class TupleTypeNodeLocations(
    val separators: List<Location>
) : NodeLocations

data class ConstrainedTypeNodeLocations(
    val separator: Location
) : NodeLocations

data class UnionTypeNodeLocations(
    val separators: List<Location>
) : NodeLocations

data class IntersectionTypeNodeLocations(
    val separators: List<Location>
) : NodeLocations

data class TypeDeclarationNodeLocations(
    val name: Location,
    val separator: Location,
    val params: List<Location>
) : NodeLocations

data class TraitFunctionNode(
    val name: String,
    val type: NodeData,
    override val location: Location,
    override val locations: TraitFunctionNodeLocation
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(type)
}

data class TraitFunctionsNode(
    val definitions: List<TraitFunctionNode>,
    val functions: List<FunctionNode>,
) : NodeData() {
    override val location: Location
        get() = Location.NoProvided
    override val locations: NodeLocations
        get() = NoLocationsDefined
    override val children: Sequence<NodeData>
        get() = definitions.asSequence()
}

data class TraitNode(
    val internal: Boolean,
    val annotations: List<AnnotationNode>?,
    val name: String,
    val params: List<String>,
    val definition: TraitFunctionsNode,
    override val location: Location,
    override val locations: TraitNodeLocations
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(definition)

}

data class LabelTypeNode(
    val name: String,
    val expr: TypeExpression,
    override val location: Location,
) : NodeData(), TypeExpression {
    override val locations: NodeLocations
        get() = NoLocationsDefined
    override val children: Sequence<NodeData>
        get() = sequenceOf(expr.cast())

    override val representation: String
        get() = "$name: ${expr.representation}"
}

data class UnitTypeNode(
    override val location: Location
) : NodeData(), TypeExpression {
    override val children: Sequence<NodeData>
        get() = emptySequence()

    override val locations: NodeLocations
        get() = NoLocationsDefined

    override val representation: String
        get() = "()"
}

data class ConcreteTypeNode(
    val name: String,
    override val location: Location
) : NodeData(), TypeExpression {
    override val children: Sequence<NodeData>
        get() = emptySequence()

    override val locations: NodeLocations
        get() = NoLocationsDefined

    override val representation: String
        get() = name
}

data class ParameterTypeNode(
    val name: String,
    override val location: Location
) : NodeData(), TypeExpression {
    override val children: Sequence<NodeData>
        get() = emptySequence()

    override val locations: NodeLocations
        get() = NoLocationsDefined

    override val representation: String
        get() = name
}

data class ParametricTypeNode(
    val variables: List<TypeExpression>,
    override val location: Location
) : NodeData(), TypeExpression {
    override val children: Sequence<NodeData>
        get() = variables.asSequence().cast()

    override val locations: NodeLocations
        get() = NoLocationsDefined

    override val representation: String
        get() = "(${variables.joinToString(" ") { it.representation }})"
}

data class FunctionTypeNode(
    val params: List<TypeExpression>,
    override val location: Location,
    override val locations: FunctionTypeNodeLocations
) : NodeData(), TypeExpression {
    override val children: Sequence<NodeData>
        get() = params.asSequence().cast()


    override val representation: String
        get() = "(${params.joinToString(" -> ") { it.representation }})"
}

data class TupleTypeNode(
    val types: List<TypeExpression>,
    override val location: Location,
    override val locations: TupleTypeNodeLocations
) : NodeData(), TypeExpression {
    override val children: Sequence<NodeData>
        get() = types.asSequence().cast()

    override val representation: String
        get() = "(${types.joinToString(", ") { it.representation }})"
}

data class ConstrainedTypeNode(
    val type: TypeExpression,
    val expression: NodeData,
    override val location: Location,
    override val locations: ConstrainedTypeNodeLocations
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
    override val locations: NodeLocations
        get() = NoLocationsDefined
}

data class SetElement(
    val union: Boolean,
    val expression: TypeExpression,
    override val location: Location
) : NodeData() {
    override val locations: NodeLocations
        get() = NoLocationsDefined
    override val children: Sequence<NodeData>
        get() = emptySequence()

}

data class UnionTypeNode(
    val types: List<TypeExpression>,
    override val location: Location,
    override val locations: UnionTypeNodeLocations
) : NodeData(), TypeExpression {
    override val children: Sequence<NodeData>
        get() = types.asSequence().cast()

    override val representation: String
        get() = "(${types.joinToString(" | ") { it.representation }})"
}

data class IntersectionTypeNode(
    val types: List<TypeExpression>,
    override val location: Location,
    override val locations: IntersectionTypeNodeLocations
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
    override val location: Location,
    override val locations: TypeNodeLocations
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(expr)
}

data class TypeDeclarationNode(
    val annotations: List<AnnotationNode>?,
    val name: String,
    val params: List<String>,
    val type: TypeExpression,
    override val location: Location,
    override val locations: TypeDeclarationNodeLocations
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(type as NodeData)
}
