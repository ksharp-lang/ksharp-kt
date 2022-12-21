package org.ksharp.nodes

import org.ksharp.common.Location

data class TempNode(
    val list: List<Any>
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = emptySequence()

    override val location: Location
        get() = Location.NoProvided
}