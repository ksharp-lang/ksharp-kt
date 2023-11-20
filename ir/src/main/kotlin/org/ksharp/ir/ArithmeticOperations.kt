package org.ksharp.ir

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.ir.truffle.FunctionNode
import org.ksharp.ir.truffle.arithmetic.*
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.types.FunctionType

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
) : SubNode(left.cast(), right.cast()), IrBinaryOperation

data class IrMul(
    override val attributes: Set<Attribute>,
    override val left: IrExpression,
    override val right: IrExpression,
    override val location: Location
) : MulNode(left.cast(), right.cast()), IrBinaryOperation


data class IrDiv(
    override val attributes: Set<Attribute>,
    override val left: IrExpression,
    override val right: IrExpression,
    override val location: Location
) : DivNode(left.cast(), right.cast()), IrBinaryOperation

data class IrPow(
    override val attributes: Set<Attribute>,
    override val left: IrExpression,
    override val right: IrExpression,
    override val location: Location
) : PowNode(left.cast(), right.cast()), IrBinaryOperation

data class IrMod(
    override val attributes: Set<Attribute>,
    override val left: IrExpression,
    override val right: IrExpression,
    override val location: Location
) : ModNode(left.cast(), right.cast()), IrBinaryOperation

class IrArithmeticCall(
    @get:JvmName("getSymbolName") override val name: String,
    override val expr: IrBinaryOperation,
    override val type: FunctionType,
) : FunctionNode(2, expr.cast()), IrTopLevelSymbol {

    override val attributes: Set<Attribute> = expr.attributes
    override val location: Location = expr.location

}
