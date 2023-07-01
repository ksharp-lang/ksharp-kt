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

fun VarNode<SemanticInfo>.toIrSymbol(variableIndex: VariableIndex) =
    variableIndex[name]!!.let {
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

fun SemanticNode<SemanticInfo>.toIrSymbol(lookup: FunctionLookup, variableIndex: VariableIndex): IrExpression =
    when (this) {
        is ConstantNode -> toIrSymbol()
        is ApplicationNode -> toIrSymbol(lookup, variableIndex)
        is AbstractionNode -> toIrSymbol(lookup, variableIndex)
        is LetBindingNode -> TODO()
        is LetNode -> TODO()
        is VarNode -> toIrSymbol(variableIndex)
    }

val IrBoolFactory: CustomApplicationIrNode = { _, _ ->
    IrBool(
        functionName.name == "True",
        location
    )
}
