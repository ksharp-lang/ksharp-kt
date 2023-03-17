package org.ksharp.nodes.semantic

import org.ksharp.common.Location
import org.ksharp.nodes.NodeData

data class LetBindingNode<SemanticInfo>(
    val name: String,
    val expression: SemanticNode<SemanticInfo>,
    override val info: SemanticInfo,
    override val location: Location,
) : SemanticNode<SemanticInfo>() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(expression)

}

data class LetNode<SemanticInfo>(
    val bindings: List<LetBindingNode<SemanticInfo>>,
    val expression: SemanticNode<SemanticInfo>,
    override val info: SemanticInfo,
    override val location: Location
) : SemanticNode<SemanticInfo>() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(bindings.asSequence(), sequenceOf(expression)).flatten()
}