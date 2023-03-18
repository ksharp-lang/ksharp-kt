package org.ksharp.semantics.expressions

import org.ksharp.nodes.ExpressionParserNode
import org.ksharp.nodes.semantic.SemanticNode

internal fun ExpressionParserNode.toSemanticNode(): SemanticNode<String> =
    when (this) {
        else -> TODO()
    }