package org.ksharp.ir

import org.ksharp.common.Location
import org.ksharp.ir.truffle.DecimalNode
import org.ksharp.ir.truffle.IntegerNode
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.CommonAttribute

sealed interface Literal : IrExpression {
    override val attributes: Set<Attribute>
        get() = setOf(CommonAttribute.Constant)

}

data class IrInteger(
    val value: Long,
    override val location: Location
) : IntegerNode(value), Literal

data class IrDecimal(
    val value: Double,
    override val location: Location
) : DecimalNode(value), Literal

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
