package ksharp.nodes

import org.ksharp.common.Location

data class ImportNode(
    val moduleName: String,
    val key: String,
    override val location: Location
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = emptySequence()

}