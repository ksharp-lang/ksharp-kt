package org.ksharp.ir.transform

import org.ksharp.common.cast
import org.ksharp.ir.Argument
import org.ksharp.ir.IrFunction
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.semantics.nodes.AbstractionSemanticInfo
import org.ksharp.semantics.nodes.SemanticInfo
import org.ksharp.semantics.nodes.Symbol
import org.ksharp.typesystem.attributes.CommonAttribute

fun AbstractionNode<SemanticInfo>.toIrSymbol(): IrFunction {
    val expression = expression.toIrSymbol()
    return IrFunction(
        expression.addExpressionAttributes(attributes, CommonAttribute.Constant, CommonAttribute.Impure),
        name,
        info.cast<AbstractionSemanticInfo>().parameters.map {
            Argument(
                it.cast<Symbol>().name,
                it.getInferredType(location).valueOrNull!!
            )
        },
        inferredType,
        expression,
        location
    )
}
