package org.ksharp.semantics.nodes

import org.ksharp.nodes.NodeData
import org.ksharp.typesystem.types.Type

data class ExprSemanticNode(
    val expr: NodeData,
    val type: Type
)