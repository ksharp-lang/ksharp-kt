package org.ksharp.ir.transform

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.ir.*
import org.ksharp.nodes.semantic.*
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.types.FunctionType
import org.ksharp.typesystem.types.arity

val pureArgument = setOf(CommonAttribute.Pure)

fun AbstractionNode<SemanticInfo>.abstractionToIrSymbol(
    partialState: PartialIrState
): IrFunction {
    val arguments = info.cast<AbstractionSemanticInfo>().parameters.filter {
        it.getType(Location.NoProvided).valueOrNull!!.representation != "Unit"
    }.map {
        it.cast<Symbol>().name
    }
    val variableIndex = if (arguments.isEmpty()) {
        emptyVariableIndex
    } else arguments.mapIndexed { index, argument ->
        argument to VarInfo(
            index,
            VarKind.Arg,
            pureArgument
        )
    }.toMap()
        .let {
            argIndex(it)
        }
    val irState = partialState.toIrState(mutableVariableIndexes(variableIndex))
    val expression = expression.toIrSymbol(irState)
    return IrFunction(
        //all functions are pure, except if it is marked impure
        expression.addExpressionAttributes(attributes, CommonAttribute.Constant, CommonAttribute.Impure),
        "$name/${inferredType.cast<FunctionType>().arguments.arity}",
        arguments,
        irState.variableIndex.size,
        expression,
        location
    )
}
