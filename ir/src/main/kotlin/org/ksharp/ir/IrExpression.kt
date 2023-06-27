package org.ksharp.ir

import org.ksharp.common.Location
import org.ksharp.typesystem.attributes.Attribute

sealed interface IrExpression : IrSymbol

sealed interface  IrBinaryOperation : IrExpression {
    val left: IrExpression
    val right: IrExpression
}

data class IrPair(
    override val attributes: Set<Attribute>,
    val first: IrExpression,
    val second: IrExpression,
    override val location: Location
) : IrExpression

data class IrVar(
    override val attributes: Set<Attribute>,
    val index: Int,
    override val location: Location
) : IrExpression
