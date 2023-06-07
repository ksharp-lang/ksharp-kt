package org.ksharp.nodes

import org.ksharp.common.Location


data class LetExpressionNode(
    val matches: List<MatchAssignNode>,
    val expression: NodeData,
    override val location: Location
) : NodeData(), ExpressionParserNode {

    override val children: Sequence<NodeData>
        get() = sequenceOf(matches, listOf(expression)).flatten()

    override val locations: NodeLocations
        get() = NoLocationsDefined

}
