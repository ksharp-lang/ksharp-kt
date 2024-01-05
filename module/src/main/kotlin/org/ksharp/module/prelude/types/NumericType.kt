package org.ksharp.module.prelude.types

import org.ksharp.common.Either
import org.ksharp.common.HandlePromise
import org.ksharp.module.prelude.preludeTypeSystem
import org.ksharp.module.prelude.serializer.TypeSerializers
import org.ksharp.module.prelude.unification.TypeUnifications
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.serializer.TypeSerializer
import org.ksharp.typesystem.solver.Solver
import org.ksharp.typesystem.solver.Solvers
import org.ksharp.typesystem.substitution.Substitution
import org.ksharp.typesystem.substitution.Substitutions
import org.ksharp.typesystem.types.ParametricTypeParam
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.unification.TypeUnification

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
) : ParametricTypeParam {
    override val typeSystem: HandlePromise<TypeSystem>
        get() = preludeTypeSystem.value.handle

    override val attributes: Set<Attribute>
        get() = NoAttributes

    override val solver: Solver
        get() = Solvers.PassThrough

    override val serializer: TypeSerializer
        get() = TypeSerializers.NumericType

    override val unification: TypeUnification
        get() = TypeUnifications.Numeric

    override val substitution: Substitution
        get() = Substitutions.Identity

    override val compound: Boolean = false
    override val terms: Sequence<Type> = emptySequence()
    override val representation: String = type.name

    override fun toString(): String = representation
    override fun new(attributes: Set<Attribute>): Type = this
}

fun numeric(type: Numeric) = Either.Right(NumericType(type))
