package org.ksharp.nodes.semantic

import org.ksharp.common.Location
import org.ksharp.nodes.NodeData
import org.ksharp.typesystem.attributes.Attribute

data class AbstractionNode<SemanticInfo>(
    val attributes: Set<Attribute>,
    val name: String,
    val expression: SemanticNode<SemanticInfo>,
    override val info: SemanticInfo,
    override val location: Location
) : SemanticNode<SemanticInfo>() {

    override val children: Sequence<NodeData>
        get() = sequenceOf(expression)

}
