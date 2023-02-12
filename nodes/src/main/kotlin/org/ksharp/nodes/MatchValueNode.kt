package org.ksharp.nodes

import org.ksharp.common.Location

enum class MatchValueType {
    Expression,
    List,
}

data class MatchValueNode(
    val type: MatchValueType,
    val value: NodeData,
    override val location: Location
) : NodeData() {

    override val children: Sequence<NodeData>
        get() = sequenceOf(value)
}

data class MatchListValueNode(
    val head: List<NodeData>,
    val tail: NodeData,
    override val location: Location
) : NodeData() {

    override val children: Sequence<NodeData>
        get() = sequenceOf(head, listOf(tail)).flatten()

}