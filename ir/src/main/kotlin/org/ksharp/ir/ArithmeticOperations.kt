package org.ksharp.ir

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.ir.serializer.IrNodeSerializers
import org.ksharp.ir.truffle.FunctionNode
import org.ksharp.ir.truffle.arithmetic.*
import org.ksharp.typesystem.attributes.Attribute

data class IrSum(
    override val attributes: Set<Attribute>,
    override val left: IrExpression,
    override val right: IrExpression,
    override val location: Location
) : SumNode(left.cast(), right.cast()), IrBinaryOperation {
    override val serializer: IrNodeSerializers
        get() = IrNodeSerializers.Sum

}


data class IrSub(
    override val attributes: Set<Attribute>,
    override val left: IrExpression,
    override val right: IrExpression,
    override val location: Location
) : SubNode(left.cast(), right.cast()), IrBinaryOperation {
    override val serializer: IrNodeSerializers
        get() = IrNodeSerializers.Sub

}

data class IrMul(
    override val attributes: Set<Attribute>,
    override val left: IrExpression,
    override val right: IrExpression,
    override val location: Location
) : MulNode(left.cast(), right.cast()), IrBinaryOperation {
    override val serializer: IrNodeSerializers
        get() = IrNodeSerializers.Mul

}


data class IrDiv(
    override val attributes: Set<Attribute>,
    override val left: IrExpression,
    override val right: IrExpression,
    override val location: Location
) : DivNode(left.cast(), right.cast()), IrBinaryOperation {
    override val serializer: IrNodeSerializers
        get() = IrNodeSerializers.Div

}

data class IrPow(
    override val attributes: Set<Attribute>,
    override val left: IrExpression,
    override val right: IrExpression,
    override val location: Location
) : PowNode(left.cast(), right.cast()), IrBinaryOperation {
    override val serializer: IrNodeSerializers
        get() = IrNodeSerializers.Pow

}

data class IrMod(
    override val attributes: Set<Attribute>,
    override val left: IrExpression,
    override val right: IrExpression,
    override val location: Location
) : ModNode(left.cast(), right.cast()), IrBinaryOperation {
    override val serializer: IrNodeSerializers
        get() = IrNodeSerializers.Mod

}

data class IrArithmeticCall(
    @get:JvmName("getSymbolName") override val name: String,
    override val expr: IrBinaryOperation,
) : FunctionNode(2, expr.cast()), IrTopLevelSymbol {

    override val attributes: Set<Attribute> = expr.attributes
    override val location: Location = expr.location
    override val arity: Int = 2
    override val serializer: IrNodeSerializers = IrNodeSerializers.ArithmeticCall

}
