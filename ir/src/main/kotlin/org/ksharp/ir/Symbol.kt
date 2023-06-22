package org.ksharp.ir

import org.ksharp.common.Location
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.types.Type

data class Argument(
    val name: String,
    val type: Type
) : IrNode

interface Symbol : IrNode {
    val location: Location
    val attributes: Set<Attribute>
}

interface TopLevelSymbol : Symbol {
    val name: String
    val expr: Expression
}

data class Function(
    override val attributes: Set<Attribute>,
    override val name: String,
    val arguments: List<Argument>,
    val type: Type,
    override val expr: Expression,
    override val location: Location
) : TopLevelSymbol, Expression

data class Type(
    override val attributes: Set<Attribute>,
    override val name: String,
    val parameters: List<String>,
    override val expr: Expression,
    override val location: Location
) : TopLevelSymbol
