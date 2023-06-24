package org.ksharp.ir.transform

import org.ksharp.common.cast
import org.ksharp.ir.*
import org.ksharp.nodes.semantic.*
import org.ksharp.semantics.nodes.SemanticInfo

fun ConstantNode<SemanticInfo>.toIrSymbol() =
    when (value) {
        is Long -> IrInteger(
            value.cast(),
            inferredType,
            location
        )

        is Double -> IrDecimal(
            value.cast(),
            inferredType,
            location
        )

        is Char -> IrCharacter(
            value.cast(),
            inferredType,
            location
        )

        is String -> IrString(
            value.cast(),
            inferredType,
            location
        )

        else -> TODO("Constant node value not supported $value: ${value.javaClass}")
    }

fun SemanticNode<SemanticInfo>.toIrSymbol(): IrExpression =
    when (this) {
        is ConstantNode -> toIrSymbol()
        is ApplicationNode -> TODO()
        is AbstractionNode -> toIrSymbol()
        is LetBindingNode -> TODO()
        is LetNode -> TODO()
        is VarNode -> TODO()
    }
