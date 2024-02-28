package org.ksharp.ir

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.ir.serializer.IrNodeSerializers
import org.ksharp.ir.truffle.FunctionNode
import org.ksharp.ir.truffle.LambdaNode
import org.ksharp.ir.truffle.variable.CaptureVarNode
import org.ksharp.typesystem.attributes.Attribute

interface IrSymbol : IrNode {
    val location: Location
    val attributes: Set<Attribute>
}

interface IrTopLevelSymbol : IrSymbol {
    val name: String
    val expr: IrExpression
}

data class IrFunction(
    override val attributes: Set<Attribute>,
    @get:JvmName("getSymbolName") override val name: String,
    val arguments: List<String>,
    val frameSlots: Int,
    override val expr: IrExpression,
    override val location: Location
) : FunctionNode(frameSlots, null, expr.cast()), IrTopLevelSymbol, IrExpression {
    fun call(vararg arguments: Any): Any = KValue.value(callTarget.call(*arguments))

    override val serializer: IrNodeSerializers = IrNodeSerializers.Function
}

data class IrCaptureVar(
    val name: String,
    val variable: IrValueAccess,
) : CaptureVarNode(name, variable.cast()), IrExpression {
    override val attributes: Set<Attribute> = variable.attributes
    override val location: Location = variable.location
    override val serializer: IrNodeSerializers = IrNodeSerializers.CaptureVar
}

data class IrLambda(
    override val attributes: Set<Attribute>,
    val capturedContext: List<IrCaptureVar>,
    val arguments: List<String>,
    val frameSlots: Int,
    val expr: IrExpression,
    override val location: Location
) : LambdaNode(frameSlots, capturedContext.cast<List<CaptureVarNode>>().toTypedArray(), expr.cast()), IrExpression {
    override val serializer: IrNodeSerializers = IrNodeSerializers.Lambda
}
