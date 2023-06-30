package org.ksharp.ir

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.ir.truffle.FunctionNode
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.types.Type

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
    val type: Type,
    override val expr: IrExpression,
    override val location: Location
) : FunctionNode(expr.cast()), IrTopLevelSymbol, IrExpression {
    fun call(vararg arguments: Any): Any = callTarget.call(arguments)
}
