package org.ksharp.ir.transform

import org.ksharp.ir.*
import org.ksharp.nodes.semantic.LetBindingNode
import org.ksharp.nodes.semantic.LetNode
import org.ksharp.nodes.semantic.VarNode
import org.ksharp.semantics.nodes.SemanticInfo
import org.ksharp.typesystem.attributes.CommonAttribute
import java.util.concurrent.atomic.AtomicInteger

fun LetBindingNode<SemanticInfo>.toIrSymbol(
    lookup: FunctionLookup,
    variableIndex: MutableVariableIndex,
    bindingCounter: AtomicInteger
): IrExpression {
    val expr = expression.toIrSymbol(lookup, variableIndex)
    return when (val m = match) {
        is VarNode -> {
            VarInfo(
                bindingCounter.incrementAndGet(),
                VarKind.Var,
                expr.attributes
            ).let {
                variableIndex[m.name] = it
                IrSetVar(expr.attributes, it.index, expr, location)
            }
        }

        else -> TODO("$match")
    }
}

fun LetNode<SemanticInfo>.toIrSymbol(lookup: FunctionLookup, variableIndex: VariableIndex): IrExpression {
    val bindingCounter = AtomicInteger(variableIndex.size - 1)
    val letVariableIndex = mutableVariableIndexes(variableIndex)
    return sequenceOf(
        bindings.map {
            it.toIrSymbol(lookup, letVariableIndex, bindingCounter)
        }.asSequence(),
        sequenceOf(expression.toIrSymbol(lookup, letVariableIndex))
    ).flatten()
        .toList()
        .let {
            val constantCounter = AtomicInteger()
            val pureCounter = AtomicInteger()
            it.forEach { e ->
                val attrs = e.attributes
                val isConstant = attrs.contains(CommonAttribute.Constant)
                if (isConstant) {
                    constantCounter.incrementAndGet()
                }
                if (isConstant || attrs.contains(CommonAttribute.Pure)) {
                    pureCounter.incrementAndGet()
                }
            }
            IrLet(computeAttributes(it.size, constantCounter.get(), pureCounter.get()), it, location)
        }
}
