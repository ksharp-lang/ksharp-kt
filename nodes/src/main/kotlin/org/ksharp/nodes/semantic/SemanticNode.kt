package org.ksharp.nodes.semantic

import org.ksharp.nodes.NoLocationsDefined
import org.ksharp.nodes.NodeData
import org.ksharp.nodes.NodeLocations

sealed class SemanticNode<SemanticInfo> : NodeData() {
    abstract val info: SemanticInfo
    override val locations: NodeLocations
        get() = NoLocationsDefined
}
