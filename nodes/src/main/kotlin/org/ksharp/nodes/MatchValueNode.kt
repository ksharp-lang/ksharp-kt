package org.ksharp.nodes

import org.ksharp.common.Location

enum class MatchValueType {
    Variable,
    Literal,
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