package org.ksharp.ir

import org.ksharp.common.Location
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.CommonAttribute

sealed interface Literal : IrExpression {
    override val attributes: Set<Attribute>
        get() = setOf(CommonAttribute.Constant)

}

data class IrInteger(
    val value: Long,
    override val location: Location
) : Literal

data class IrDecimal(
    val value: Double,
    override val location: Location
) : Literal

data class IrCharacter(
    val value: Char,
    override val location: Location
) : Literal

data class IrString(
    val value: String,
    override val location: Location
) : Literal

data class IrBool(
    val value: Boolean,
    override val location: Location
) : Literal
