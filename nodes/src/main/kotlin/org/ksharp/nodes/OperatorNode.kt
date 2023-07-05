package org.ksharp.nodes

import org.ksharp.common.Location

data class OperatorNode(
    val category: String,
    val operator: String,
    val left: NodeData,
    val right: NodeData,
    override val location: Location,
) : NodeData(), ExpressionParserNode {
    override val locations: NodeLocations
        get() = NoLocationsDefined
    override val children: Sequence<NodeData>
        get() = sequenceOf(left, right)
}
