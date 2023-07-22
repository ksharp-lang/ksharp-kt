package org.ksharp.ir.transform

import org.ksharp.ir.IrExpression
import org.ksharp.ir.IrLet
import org.ksharp.ir.IrSetVar
import org.ksharp.ir.IrState
import org.ksharp.nodes.semantic.LetBindingNode
import org.ksharp.nodes.semantic.LetNode
import org.ksharp.nodes.semantic.VarNode
import org.ksharp.semantics.nodes.SemanticInfo
import org.ksharp.typesystem.attributes.CommonAttribute
import java.util.concurrent.atomic.AtomicInteger

fun LetBindingNode<SemanticInfo>.toIrSymbol(
    state: IrState
): IrExpression {
    val expr = expression.toIrSymbol(state)
    return when (val m = match) {
        is VarNode -> {
            state.addVariable(
                m.name,
                expr.attributes
            ).let {
                IrSetVar(expr.attributes, it.index, expr, location)
            }
        }

        else -> TODO("$match")
    }
}

fun LetNode<SemanticInfo>.toIrSymbol(state: IrState): IrExpression {
    return sequenceOf(
        bindings.map {
            it.toIrSymbol(state)
        }.asSequence(),
        sequenceOf(expression.toIrSymbol(state))
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
