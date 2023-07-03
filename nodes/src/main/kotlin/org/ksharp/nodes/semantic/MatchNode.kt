package org.ksharp.nodes.semantic

import org.ksharp.common.Location
import org.ksharp.nodes.MatchValueType
import org.ksharp.nodes.NodeData

data class ConditionalMatchValueNode<SemanticInfo>(
    val condition: MatchValueType,
    val match: SemanticNode<SemanticInfo>,
    override val info: SemanticInfo,
    override val location: Location
) : SemanticNode<SemanticInfo>() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(match)

}

data class ListMatchValueNode<SemanticInfo>(
    val head: List<SemanticNode<SemanticInfo>>,
    val tail: SemanticNode<SemanticInfo>,
    override val info: SemanticInfo,
    override val location: Location
) : SemanticNode<SemanticInfo>() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(head, listOf(tail)).flatten()

}

data class GroupMatchValueNode<SemanticInfo>(
    val matches: List<SemanticNode<SemanticInfo>>,
    override val info: SemanticInfo,
    override val location: Location
) : SemanticNode<SemanticInfo>() {
    override val children: Sequence<NodeData>
        get() = matches.asSequence()

}


data class MatchBranchNode<SemanticInfo>(
    val matches: List<SemanticNode<SemanticInfo>>,
    val expression: SemanticNode<SemanticInfo>,
    override val info: SemanticInfo,
    override val location: Location
) : SemanticNode<SemanticInfo>() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(matches, listOf(expression)).flatten()
}

data class MatchNode<SemanticInfo>(
    val branches: List<MatchBranchNode<SemanticInfo>>,
    val expression: SemanticNode<SemanticInfo>,
    override val info: SemanticInfo,
    override val location: Location
) : SemanticNode<SemanticInfo>() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(listOf(expression), branches).flatten()

}
