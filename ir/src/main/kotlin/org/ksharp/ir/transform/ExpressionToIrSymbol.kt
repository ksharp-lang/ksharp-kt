package org.ksharp.ir.transform

import org.ksharp.common.cast
import org.ksharp.ir.*
import org.ksharp.nodes.semantic.*
import org.ksharp.semantics.nodes.SemanticInfo

fun ConstantNode<SemanticInfo>.toIrSymbol(): IrExpression =
    when (value) {
        is Long -> IrInteger(
            value.cast(),
            location
        )

        is Double -> IrDecimal(
            value.cast(),
            location
        )

        is Char -> IrCharacter(
            value.cast(),
            location
        )

        is String -> IrString(
            value.cast(),
            location
        )

        else -> TODO("Constant node value not supported $value: ${value.javaClass}")
    }

fun VarNode<SemanticInfo>.toIrSymbol(state: IrState): IrExpression =
    state.variableIndex[name]!!.let {
        when (it.kind) {
            VarKind.Arg -> IrArg(
                it.attributes,
                it.index,
                location
            )

            VarKind.Var -> IrVar(
                it.attributes,
                it.index,
                location
            )
        }
    }

fun SemanticNode<SemanticInfo>.toIrSymbol(state: IrState): IrExpression =
    when (this) {
        is ConstantNode -> toIrSymbol()
        is VarNode -> toIrSymbol(state)
        is ApplicationNode -> toIrSymbol(state)
        is AbstractionNode -> toIrSymbol(state.functionLookup)
        is LetNode -> toIrSymbol(state)

        else -> TODO("$this")
    }

val IrBoolFactory: CustomApplicationIrNode = { _ ->
    IrBool(
        functionName.name == "True",
        location
    )
}
