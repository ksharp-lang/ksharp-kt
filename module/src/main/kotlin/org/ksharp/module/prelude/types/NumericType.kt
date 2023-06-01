package org.ksharp.module.prelude.types

import org.ksharp.common.Either
import org.ksharp.module.RecordSize
import org.ksharp.module.prelude.serializer.TypeSerializers
import org.ksharp.module.prelude.unification.TypeUnifications
import org.ksharp.typesystem.serializer.TypeSerializer
import org.ksharp.typesystem.substitution.Substitution
import org.ksharp.typesystem.substitution.Substitutions
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.types.TypeVisibility
import org.ksharp.typesystem.unification.TypeUnification

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

    override val visibility: TypeVisibility
        get() = TypeVisibility.Public

    override val serializer: TypeSerializer
        get() = TypeSerializers.NumericType

    override val unification: TypeUnification
        get() = TypeUnifications.Numeric

    override val substitution: Substitution
        get() = Substitutions.Identity

    override val compound: Boolean = false
    override val terms: Sequence<Type> = emptySequence()
    override val representation: String = "numeric<${type}>"

    override fun toString(): String = representation
}

fun numeric(type: Numeric) = Either.Right(NumericType(type))
