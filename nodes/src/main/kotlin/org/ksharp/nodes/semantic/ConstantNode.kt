package org.ksharp.nodes.semantic

import org.ksharp.common.Location
import org.ksharp.nodes.LiteralValueType
import org.ksharp.nodes.NodeData

data class ConstantNode<SemanticInfo>(
    val value: String,
    val constantType: LiteralValueType,
    override val info: SemanticInfo,
    override val location: Location
) : SemanticNode<SemanticInfo>() {
    override val children: Sequence<NodeData>
        get() = emptySequence()
    
}