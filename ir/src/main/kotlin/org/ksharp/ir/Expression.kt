package org.ksharp.ir

import org.ksharp.common.Location
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.CommonAttribute

sealed interface Expression : IrSymbol

data class IrVariableAccess(
    override val attributes: Set<Attribute>,
    val index: Int,
    override val location: Location
) : Expression

data class IrUnit(
    override val location: Location
) : Expression {
    override val attributes: Set<Attribute>
        get() = setOf(CommonAttribute.Constant)
}
