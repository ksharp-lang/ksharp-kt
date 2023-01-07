package org.ksharp.nodes

import org.ksharp.common.Location

data class TraitFunctionNode(
    val name: String,
    val type: NodeData,
    override val location: Location
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(type)

}

data class TraitFunctionsNode(
    val functions: List<TraitFunctionNode>
) : NodeData() {
    override val location: Location
        get() = Location.NoProvided
    override val children: Sequence<NodeData>
        get() = functions.asSequence()
}

data class TypeNode(
    val internal: Boolean,
    val name: String,
    val params: List<String>,
    val expr: NodeData,
    override val location: Location
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(expr)
}