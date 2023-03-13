package org.ksharp.module.prelude.types

import org.ksharp.common.Either
import org.ksharp.module.RecordSize
import org.ksharp.typesystem.types.ParametricTypeFactory
import org.ksharp.typesystem.types.Type

enum class Numeric(val size: kotlin.Int, val isInteger: Boolean, val recordSize: RecordSize) {
    Byte(8, true, RecordSize.Single),
    Short(16, true, RecordSize.Single),
    Int(32, true, RecordSize.Single),
    Long(64, true, RecordSize.Double),
    BigInt(128, true, RecordSize.Single),
    Float(32, false, RecordSize.Single),
    Double(64, false, RecordSize.Double),
    BigDecimal(128, false, RecordSize.Single)
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