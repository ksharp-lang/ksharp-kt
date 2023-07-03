package org.ksharp.nodes

import org.ksharp.common.Location

enum class MatchValueType {
    Expression,
    List,
    Or,
    And,
    Group,
}

data class MatchListValueNodeLocations(
    val tailSeparatorLocation: Location
) : NodeLocations

data class MatchAssignNodeLocations(
    val assignOperatorLocation: Location
) : NodeLocations

data class MatchValueNode(
    val type: MatchValueType,
    val value: NodeData,
    override val location: Location
) : NodeData(), ExpressionParserNode {

    override val children: Sequence<NodeData>
        get() = sequenceOf(value)

    override val locations: NodeLocations
        get() = NoLocationsDefined
}

data class MatchListValueNode(
    val head: List<NodeData>,
    val tail: NodeData,
    override val location: Location,
    override val locations: MatchListValueNodeLocations
) : NodeData(), ExpressionParserNode {

    override val children: Sequence<NodeData>
        get() = sequenceOf(head, listOf(tail)).flatten()

}

data class GroupMatchValueNode(
    val matches: List<MatchValueNode>,
    override val location: Location
) :
    NodeData(), ExpressionParserNode {

    override val locations: NodeLocations
        get() = NoLocationsDefined
    override val children: Sequence<NodeData>
        get() = matches.asSequence()

}

data class MatchAssignNode(
    val matchValue: MatchValueNode,
    val expression: NodeData,
    override val location: Location,
    override val locations: MatchAssignNodeLocations
) : NodeData(), ExpressionParserNode {
    override val children: Sequence<NodeData>
        get() = sequenceOf(matchValue, expression)
}

data class MatchExpressionBranchNode(
    val matchValue: List<MatchValueNode>,
    val expression: NodeData,
    override val location: Location
) : NodeData(), ExpressionParserNode {
    override val children: Sequence<NodeData>
        get() = sequenceOf(matchValue.asSequence(), sequenceOf(expression)).flatten()

    override val locations: NodeLocations
        get() = NoLocationsDefined
}
