package org.ksharp.ir

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.ir.serializer.IrNodeSerializers
import org.ksharp.ir.truffle.FunctionNode
import org.ksharp.typesystem.attributes.Attribute

interface IrSymbol : IrNode {
    val location: Location
    val attributes: Set<Attribute>
}

interface IrTopLevelSymbol : IrSymbol {
    val name: String
    val arity: Int
    val expr: IrExpression
}

data class IrFunction(
    override val attributes: Set<Attribute>,
    @get:JvmName("getSymbolName") override val name: String,
    override val arity: Int,
    val arguments: List<String>,
    val frameSlots: Int,
    override val expr: IrExpression,
    override val location: Location
) : FunctionNode(frameSlots, expr.cast()), IrTopLevelSymbol, IrExpression {
    fun call(vararg arguments: Any): Any = callTarget.call(*arguments)

    override val serializer: IrNodeSerializers = IrNodeSerializers.NoDefined
}
