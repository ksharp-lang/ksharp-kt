package org.ksharp.nodes

import org.ksharp.common.Location

enum class FunctionType {
    Operator,
    Function,
    TypeInstance
}

data class FunctionCallNode(
    val name: String,
    val type: FunctionType,
    val arguments: List<NodeData>,
    override val location: Location
) : NodeData(), ExpressionParserNode {
    override val locations: NodeLocations get() = NoLocationsDefined
    override val children: Sequence<NodeData>
        get() = arguments.asSequence()
}
