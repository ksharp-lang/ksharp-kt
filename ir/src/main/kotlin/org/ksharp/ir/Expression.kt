package org.ksharp.ir

import org.ksharp.typesystem.types.Type

interface Expression : IrNode {
    val attributes: Set<Attribute>
}

data class ConstantExpression(
    override val attributes: Set<Attribute>,
    val value: Any,
    val type: Type
) : Expression

data class VariableAccessExpression(
    override val attributes: Set<Attribute>,
    val index: Int
) : Expression
