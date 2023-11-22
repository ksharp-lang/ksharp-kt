package org.ksharp.nodes.semantic

import org.ksharp.common.Location
import org.ksharp.nodes.NodeData
import org.ksharp.typesystem.types.FunctionType

data class ApplicationName(
    val pck: String? = null,
    val name: String
)

data class ApplicationSemanticInfo(var function: FunctionType? = null) : SemanticInfo()

data class ApplicationNode<SemanticInfo>(
    val functionName: ApplicationName,
    val arguments: List<SemanticNode<SemanticInfo>>,
    override val info: SemanticInfo,
    override val location: Location
) : SemanticNode<SemanticInfo>() {
    override val children: Sequence<NodeData>
        get() = arguments.asSequence()

}
