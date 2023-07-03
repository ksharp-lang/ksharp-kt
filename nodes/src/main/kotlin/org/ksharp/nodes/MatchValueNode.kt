package org.ksharp.nodes

import org.ksharp.common.Location

enum class MatchConditionalType {
    Or,
    And
}

data class MatchListValueNodeLocations(
    val tailSeparatorLocation: Location
) : NodeLocations

data class MatchListValueNode(
    val head: List<NodeData>,
    val tail: NodeData,
    override val location: Location,
    override val locations: MatchListValueNodeLocations
) : NodeData(), ExpressionParserNode {

    override val children: Sequence<NodeData>
        get() = sequenceOf(head, listOf(tail)).flatten()

}

data class MatchConditionValueNode(
    val type: MatchConditionalType,
    val left: NodeData,
    val right: NodeData,
    override val location: Location
) : NodeData(), ExpressionParserNode {

    override val locations: NodeLocations
        get() = NoLocationsDefined
    override val children: Sequence<NodeData>
        get() = sequenceOf(left, right)

}

data class MatchAssignNode(
    val match: NodeData,
    val expression: NodeData,
    override val location: Location
) : NodeData(), ExpressionParserNode {

    override val locations: NodeLocations
        get() = NoLocationsDefined
    override val children: Sequence<NodeData>
        get() = sequenceOf(match, expression)
}

data class MatchExpressionBranchNode(
    val match: NodeData,
    val expression: NodeData,
    override val location: Location
) : NodeData(), ExpressionParserNode {
    override val children: Sequence<NodeData>
        get() = sequenceOf(match, expression)

    override val locations: NodeLocations
        get() = NoLocationsDefined
}
