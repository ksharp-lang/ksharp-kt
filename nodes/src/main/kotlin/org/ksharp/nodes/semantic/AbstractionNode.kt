package org.ksharp.nodes.semantic

import org.ksharp.common.Location
import org.ksharp.nodes.NodeData
import org.ksharp.typesystem.annotations.Annotation

data class AbstractionNode<SemanticInfo>(
    val native: Boolean,
    val annotations: List<Annotation>?,
    val name: String,
    val expression: SemanticNode<SemanticInfo>,
    override val info: SemanticInfo,
    override val location: Location
) : SemanticNode<SemanticInfo>() {

    override val children: Sequence<NodeData>
        get() = sequenceOf(expression)

}
