package org.ksharp.nodes

import org.ksharp.common.Location

data class FunctionNode(
    val pub: Boolean,
    val annotations: List<AnnotationNode>?,
    val name: String,
    val parameters: List<String>,
    val expression: NodeData,
    override val location: Location
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(expression)
}
