package org.ksharp.nodes

import org.ksharp.common.Location

data class AnnotationNode(
    val name: String,
    val attrs: Map<String, Any>,
    override val location: Location
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = emptySequence()
}
