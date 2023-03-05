package org.ksharp.semantics.scopes

import org.ksharp.nodes.NodeData
import org.ksharp.typesystem.types.Type

data class ExprSemanticNode(
    val expr: NodeData,
    val type: Type
)