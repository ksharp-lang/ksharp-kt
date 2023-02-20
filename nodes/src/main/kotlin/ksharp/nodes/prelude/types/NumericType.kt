package ksharp.nodes.prelude.types

import org.ksharp.common.Either
import org.ksharp.typesystem.types.ParametricTypeFactory
import org.ksharp.typesystem.types.Type

enum class Numeric(val size: kotlin.Int, val isInteger: Boolean) {
    Byte(8, true),
    Short(16, true),
    Int(32, true),
    Long(64, true),
    BigInt(128, true),
    Float(32, false),
    Double(64, false),
    BigDecimal(128, false)
}

data class NumericType internal constructor(
    val type: Numeric
) : Type {
    override val compound: Boolean = false
    override val terms: Sequence<Type> = emptySequence()
    override val representation: String = "numeric<${type}>"
}

fun ParametricTypeFactory.numeric(type: Numeric) =
    add { Either.Right(NumericType(type)) }