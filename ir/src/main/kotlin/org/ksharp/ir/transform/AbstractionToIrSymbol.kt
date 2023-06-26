package org.ksharp.ir.transform

import org.ksharp.common.cast
import org.ksharp.ir.*
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.semantics.nodes.AbstractionSemanticInfo
import org.ksharp.semantics.nodes.SemanticInfo
import org.ksharp.semantics.nodes.Symbol
import org.ksharp.typesystem.attributes.CommonAttribute

val pureArgument = setOf(CommonAttribute.Pure)

fun AbstractionNode<SemanticInfo>.toIrSymbol(currentVariableIndex: VariableIndex): IrFunction {
    val arguments = info.cast<AbstractionSemanticInfo>().parameters.map {
        it.cast<Symbol>().name
    }
    val variableIndex = if (arguments.isEmpty()) {
        currentVariableIndex
    } else arguments.mapIndexed { index, argument ->
        argument to VarInfo(
            index + currentVariableIndex.size,
            pureArgument
        )
    }.toMap()
        .let {
            chainVariableIndexes(variableIndex(it), currentVariableIndex)
        }
    val expression = expression.toIrSymbol(variableIndex)
    return IrFunction(
        //all functions are pure, except if it is marked impure
        expression.addExpressionAttributes(attributes, CommonAttribute.Constant, CommonAttribute.Impure),
        name,
        arguments,
        inferredType,
        expression,
        location
    )
}
