package org.ksharp.ir.transform

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.ir.*
import org.ksharp.nodes.semantic.*
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.attributes.NoAttributes

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

        else -> TODO("Constant node value not supported $value: ${this}")
    }

fun VarInfo.toIrSymbol(location: Location): IrExpression =
    when (kind) {
        VarKind.Arg -> IrArg(
            attributes,
            index,
            location
        )

        VarKind.Var -> IrVar(
            attributes,
            index,
            location
        )
    }

fun VarNode<SemanticInfo>.toIrSymbol(state: IrState): IrExpression =
    state.variableIndex[name]!!.toIrSymbol(location)

fun AbstractionLambdaNode<SemanticInfo>.toIrSymbol(state: IrState): IrExpression {
    val arguments = info.cast<AbstractionSemanticInfo>().parameters.filter {
        it.getType(Location.NoProvided).valueOrNull!!.representation != "Unit"
    }.map {
        it.cast<Symbol>().name
    }
    val variableIndex = if (arguments.isEmpty()) {
        state.variableIndex
    } else {
        val size = state.variableIndex.size
        arguments.mapIndexed { index, argument ->
            argument to VarInfo(
                size + index,
                VarKind.Arg,
                pureArgument
            )
        }.toMap()
            .let {
                closureIndex(argIndex(it), state.variableIndex)
            }
    }
    val irState = state.toIrState(mutableVariableIndexes(variableIndex))
    val expression = expression.toIrSymbol(irState)
    return IrLambda(
        //all functions are pure, except if it is marked impure
        expression.addExpressionAttributes(NoAttributes, CommonAttribute.Constant, CommonAttribute.Impure),
        arguments,
        irState.variableIndex.size,
        expression,
        location
    )
}

fun SemanticNode<SemanticInfo>.toIrSymbol(state: IrState): IrExpression =
    when (this) {
        is ConstantNode -> toIrSymbol()
        is VarNode -> toIrSymbol(state)
        is ApplicationNode -> toIrSymbol(state)
        is AbstractionNode -> abstractionToIrSymbol(state)
        is LetNode -> toIrSymbol(state)
        is AbstractionLambdaNode -> toIrSymbol(state)
        else -> TODO("$this")
    }

val IrBoolFactory: CustomApplicationIrNode = { _ ->
    IrBool(
        functionName.name == "True",
        location
    )
}
