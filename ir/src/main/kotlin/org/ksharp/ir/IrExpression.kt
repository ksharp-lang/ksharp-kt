package org.ksharp.ir

import org.ksharp.common.Location
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.CommonAttribute

sealed interface IrExpression : IrSymbol

data class IrPair(
    override val attributes: Set<Attribute>,
    val first: IrExpression,
    val second: IrExpression,
    override val location: Location
) : IrExpression

data class IrVar(
    override val attributes: Set<Attribute>,
    val index: Int,
    override val location: Location
) : IrExpression

data class IrUnit(
    override val location: Location
) : IrExpression {
    override val attributes: Set<Attribute>
        get() = setOf(CommonAttribute.Constant)
}
