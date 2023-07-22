package org.ksharp.ir

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.ir.truffle.FunctionNode
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.types.FunctionType

interface IrSymbol : IrNode {
    val location: Location
    val attributes: Set<Attribute>
}

interface IrTopLevelSymbol : IrSymbol {
    val name: String
    val expr: IrExpression
    val type: FunctionType
}

data class IrFunction(
    override val attributes: Set<Attribute>,
    @get:JvmName("getSymbolName") override val name: String,
    val arguments: List<String>,
    val frameSlots: Int,
    override val type: FunctionType,
    override val expr: IrExpression,
    override val location: Location
) : FunctionNode(frameSlots, expr.cast()), IrTopLevelSymbol, IrExpression {
    fun call(vararg arguments: Any): Any = callTarget.call(*arguments)
}
