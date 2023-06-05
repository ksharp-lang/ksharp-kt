package org.ksharp.nodes

import org.ksharp.common.Location

data class LetExpressionNodeLocations(
    val letLocation: Location,
) : NodeLocations

data class LetExpressionNode(
    val matches: List<MatchAssignNode>,
    val expression: NodeData,
    override val location: Location,
    override val locations: LetExpressionNodeLocations
) : NodeData(), ExpressionParserNode {

    override val children: Sequence<NodeData>
        get() = sequenceOf(matches, listOf(expression)).flatten()

}
