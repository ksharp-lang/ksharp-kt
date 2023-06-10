package org.ksharp.nodes

import org.ksharp.common.Location

data class ImportNodeLocations(
    val importLocation: Location,
    val moduleNameBegin: Location,
    val moduleNameEnd: Location,
    val asLocation: Location,
    val keyLocation: Location
) : NodeLocations

data class ImportNode(
    val moduleName: String,
    val key: String,
    override val location: Location,
    override val locations: ImportNodeLocations
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = emptySequence()

}
