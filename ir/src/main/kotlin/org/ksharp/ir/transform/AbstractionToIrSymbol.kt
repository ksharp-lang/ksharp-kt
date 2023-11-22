package org.ksharp.ir.transform

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.ir.*
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.nodes.semantic.AbstractionSemanticInfo
import org.ksharp.nodes.semantic.SemanticInfo
import org.ksharp.nodes.semantic.Symbol
import org.ksharp.semantics.nodes.getType
import org.ksharp.typesystem.attributes.CommonAttribute

val pureArgument = setOf(CommonAttribute.Pure)

fun AbstractionNode<SemanticInfo>.toIrSymbol(
    functionLookup: FunctionLookup
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
    val irState = IrState(functionLookup, mutableVariableIndexes(variableIndex))
    val expression = expression.toIrSymbol(irState)
    return IrFunction(
        //all functions are pure, except if it is marked impure
        expression.addExpressionAttributes(attributes, CommonAttribute.Constant, CommonAttribute.Impure),
        name,
        arguments,
        irState.variableIndex.size,
        inferredType.cast(),
        expression,
        location
    )
}
