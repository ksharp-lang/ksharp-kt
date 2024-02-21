package org.ksharp.nodes

import org.ksharp.common.Location

data class FunctionNodeLocations(
    val nativeLocation: Location,
    val pubLocation: Location,
    val name: Location,
    val parameters: List<Location>,
    val assignOperator: Location
) : NodeLocations

data class FunctionNode(
    val native: Boolean,
    val pub: Boolean,
    val annotations: List<AnnotationNode>?,
    val name: String,
    val parameters: List<String>,
    val expression: NodeData,
    override val location: Location,
    override val locations: FunctionNodeLocations
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(expression)
}

data class LambdaNodeLocations(
    val assignOperator: Location,
    val parameters: List<Location>
) : NodeLocations

data class LambdaNode(
    val parameters: List<String>,
    val expression: NodeData,
    override val location: Location,
    override val locations: LambdaNodeLocations
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(expression)
}
