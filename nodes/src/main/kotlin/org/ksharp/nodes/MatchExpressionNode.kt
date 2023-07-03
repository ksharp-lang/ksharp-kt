package org.ksharp.nodes

import org.ksharp.common.Location

data class MatchExpressionNodeLocations(
    val matchLocation: Location,
    val withLocation: Location
) : NodeLocations

data class MatchExpressionNode(
    val branches: List<MatchExpressionBranchNode>,
    val expression: NodeData,
    override val location: Location,
    override val locations: MatchExpressionNodeLocations
) : NodeData(), ExpressionParserNode {
    override val children: Sequence<NodeData>
        get() = sequenceOf(listOf(expression), branches).flatten()

}
