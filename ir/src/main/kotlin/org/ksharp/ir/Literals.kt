package org.ksharp.ir

import org.ksharp.common.Location
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.types.Type

sealed interface Literal : Expression {
    override val attributes: Set<Attribute>
        get() = setOf(CommonAttribute.Constant)

}

data class IrInteger(
    val value: Long,
    val type: Type,
    override val location: Location
) : Literal

data class IrDecimal(
    val value: Double,
    val type: Type,
    override val location: Location
) : Literal

data class IrCharacter(
    val value: Char,
    val type: Type,
    override val location: Location
) : Literal

data class IrString(
    val value: String,
    val type: Type,
    override val location: Location
) : Literal
