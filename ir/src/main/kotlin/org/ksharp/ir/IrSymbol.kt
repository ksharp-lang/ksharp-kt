package org.ksharp.ir

import org.ksharp.common.Location
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.types.Type

data class Argument(
    val name: String,
    val type: Type
)

interface IrSymbol : IrNode {
    val location: Location
    val attributes: Set<Attribute>
}

interface IrTopLevelSymbol : IrSymbol {
    val name: String
    val expr: Expression
}

data class IrFunction(
    override val attributes: Set<Attribute>,
    override val name: String,
    val arguments: List<Argument>,
    val type: Type,
    override val expr: Expression,
    override val location: Location
) : IrTopLevelSymbol, Expression

data class IrType(
    override val attributes: Set<Attribute>,
    override val name: String,
    val parameters: List<String>,
    override val expr: Expression,
    override val location: Location
) : IrTopLevelSymbol
