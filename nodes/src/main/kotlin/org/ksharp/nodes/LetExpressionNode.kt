package org.ksharp.nodes

import org.ksharp.common.Location

data class LetExpressionNode(
    val matches: List<MatchAssignNode>,
    val expression: NodeData,
    override val location: Location
) : NodeData() {

    override val children: Sequence<NodeData>
        get() = sequenceOf(matches, listOf(expression)).flatten()

}