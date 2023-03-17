package org.ksharp.nodes.semantic

import org.ksharp.common.Location
import org.ksharp.nodes.NodeData

data class ApplicationNode<SemanticInfo>(
    val functionName: String,
    val arguments: List<SemanticNode<SemanticInfo>>,
    override val info: SemanticInfo,
    override val location: Location
) : SemanticNode<SemanticInfo>() {
    override val children: Sequence<NodeData>
        get() = arguments.asSequence()
    
}