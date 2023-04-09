package org.ksharp.nodes.semantic

import org.ksharp.common.Location
import org.ksharp.nodes.NodeData

data class ConstantNode<SemanticInfo>(
    val value: Any,
    override val info: SemanticInfo,
    override val location: Location
) : SemanticNode<SemanticInfo>() {
    override val children: Sequence<NodeData>
        get() = emptySequence()

}