package org.ksharp.nodes.semantic

import org.ksharp.common.Location
import org.ksharp.nodes.NodeData

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
