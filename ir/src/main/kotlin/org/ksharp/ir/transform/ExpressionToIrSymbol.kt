package org.ksharp.ir.transform

import org.ksharp.ir.ConstantExpression
import org.ksharp.ir.Expression
import org.ksharp.nodes.semantic.*
import org.ksharp.semantics.nodes.SemanticInfo

fun ConstantNode<SemanticInfo>.toIrSymbol() =
    ConstantExpression(
        value,
        inferredType,
        location
    )

fun SemanticNode<SemanticInfo>.toIrSymbol(): Expression =
    when (this) {
        is ConstantNode -> toIrSymbol()
        is ApplicationNode -> TODO()
        is AbstractionNode -> toIrSymbol()
        is LetBindingNode -> TODO()
        is LetNode -> TODO()
        is VarNode -> TODO()
    }
