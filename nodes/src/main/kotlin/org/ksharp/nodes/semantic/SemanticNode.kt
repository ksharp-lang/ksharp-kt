package org.ksharp.nodes.semantic

import org.ksharp.nodes.NodeData

sealed class SemanticNode<SemanticInfo> : NodeData() {
    abstract val info: SemanticInfo
}