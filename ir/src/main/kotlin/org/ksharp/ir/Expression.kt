package org.ksharp.ir

import org.ksharp.common.Location
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.types.Type

sealed interface Expression : Symbol

data class ConstantExpression(
    val value: Any,
    val type: Type,
    override val location: Location
) : Expression {
    override val attributes: Set<Attribute>
        get() = setOf(CommonAttribute.Constant)
}

data class VariableAccessExpression(
    override val attributes: Set<Attribute>,
    val index: Int,
    override val location: Location
) : Expression

data class UnitExpression(
    override val location: Location
) : Expression {
    override val attributes: Set<Attribute>
        get() = setOf(CommonAttribute.Constant)
}
