package org.ksharp.nodes

import org.ksharp.common.Location

data class ModuleNode(
    val name: String,
    val imports: List<ImportNode>,
    override val location: Location
) : NodeData() {

    override val children: Sequence<NodeData>
        get() = imports.asSequence()

}