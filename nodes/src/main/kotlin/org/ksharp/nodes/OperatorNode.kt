package org.ksharp.nodes

import org.ksharp.common.Location

data class OperatorNode(
    val operator: String,
    val left: NodeData,
    val right: NodeData,
    override val location: Location
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(left, right)
}