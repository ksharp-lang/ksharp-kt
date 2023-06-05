package org.ksharp.nodes

import org.ksharp.common.Location

data class OperatorNodeLocations(
    val operatorLocation: Location
) : NodeLocations

data class OperatorNode(
    val operator: String,
    val left: NodeData,
    val right: NodeData,
    override val location: Location,
    override val locations: OperatorNodeLocations
) : NodeData(), ExpressionParserNode {
    override val children: Sequence<NodeData>
        get() = sequenceOf(left, right)
}
