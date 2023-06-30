package org.ksharp.ir

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.ir.truffle.SumNode
import org.ksharp.typesystem.attributes.Attribute

data class IrSum(
    override val attributes: Set<Attribute>,
    override val left: IrExpression,
    override val right: IrExpression,
    override val location: Location
) : SumNode(left.cast(), right.cast()), IrBinaryOperation


data class IrSub(
    override val attributes: Set<Attribute>,
    override val left: IrExpression,
    override val right: IrExpression,
    override val location: Location
) : IrBinaryOperation

data class IrMul(
    override val attributes: Set<Attribute>,
    override val left: IrExpression,
    override val right: IrExpression,
    override val location: Location
) : IrBinaryOperation


data class IrDiv(
    override val attributes: Set<Attribute>,
    override val left: IrExpression,
    override val right: IrExpression,
    override val location: Location
) : IrBinaryOperation

data class IrPow(
    override val attributes: Set<Attribute>,
    override val left: IrExpression,
    override val right: IrExpression,
    override val location: Location
) : IrBinaryOperation
