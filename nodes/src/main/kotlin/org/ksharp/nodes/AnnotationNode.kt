package org.ksharp.nodes

import org.ksharp.common.Location

data class AttributeLocation(
    val key: Location?,
    val value: Any,
    val valueLength: Int,
    val operator: Location?
)

data class AnnotationNodeLocations(
    val altLocation: Location,
    val name: Location,
    val attrs: List<AttributeLocation>
) : NodeLocations

data class AnnotationNode(
    val name: String,
    val attrs: Map<String, Any>,
    override val location: Location,
    override val locations: AnnotationNodeLocations
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = emptySequence()
}
