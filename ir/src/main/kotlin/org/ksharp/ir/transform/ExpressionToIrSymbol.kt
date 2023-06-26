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

fun VarNode<SemanticInfo>.toIrSymbol(variableIndex: VariableIndex) =
    variableIndex[name]!!.let {
        IrVar(
            it.attributes,
            it.index,
            location
        )
    }

fun SemanticNode<SemanticInfo>.toIrSymbol(variableIndex: VariableIndex): IrExpression =
    when (this) {
        is ConstantNode -> toIrSymbol()
        is ApplicationNode -> toIrSymbol(variableIndex)
        is AbstractionNode -> toIrSymbol(variableIndex)
        is LetBindingNode -> TODO()
        is LetNode -> TODO()
        is VarNode -> toIrSymbol(variableIndex)
    }
