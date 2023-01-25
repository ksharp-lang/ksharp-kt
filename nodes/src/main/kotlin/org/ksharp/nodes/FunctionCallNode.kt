package org.ksharp.nodes

import org.ksharp.common.Location

enum class FunctionType {
    Operator,
    Function
}

data class FunctionCallNode(
    val type: FunctionType,
    val name: String,
    val arguments: List<NodeData>,
    override val location: Location
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = arguments.asSequence()
}