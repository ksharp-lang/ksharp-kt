package org.ksharp.ir

import org.ksharp.common.Location
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
    override val name: String,
    val arguments: List<String>,
    val type: Type,
    override val expr: IrExpression,
    override val location: Location
) : IrTopLevelSymbol, IrExpression

data class IrType(
    override val attributes: Set<Attribute>,
    override val name: String,
    val parameters: List<String>,
    override val expr: IrExpression,
    override val location: Location
) : IrTopLevelSymbol
