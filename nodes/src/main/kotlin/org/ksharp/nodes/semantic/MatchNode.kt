package org.ksharp.nodes.semantic

import org.ksharp.common.Location
import org.ksharp.common.TableBuilder
import org.ksharp.nodes.MatchConditionalType
import org.ksharp.nodes.NodeData

data class MatchSemanticInfo(
    val table: TableBuilder<Symbol>,
) : SemanticInfo()


data class ConditionalMatchValueNode<SemanticInfo>(
    val type: MatchConditionalType,
    val left: SemanticNode<SemanticInfo>,
    val right: SemanticNode<SemanticInfo>,
    override val info: SemanticInfo,
    override val location: Location
) : SemanticNode<SemanticInfo>() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(left, right)

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

data class MatchBranchNode<SemanticInfo>(
    val match: SemanticNode<SemanticInfo>,
    val expression: SemanticNode<SemanticInfo>,
    override val info: SemanticInfo,
    override val location: Location
) : SemanticNode<SemanticInfo>() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(match, expression)
}

data class MatchNode<SemanticInfo>(
    val expression: SemanticNode<SemanticInfo>,
    val branches: List<MatchBranchNode<SemanticInfo>>,
    override val info: SemanticInfo,
    override val location: Location
) : SemanticNode<SemanticInfo>() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(listOf(expression), branches).flatten()

}
