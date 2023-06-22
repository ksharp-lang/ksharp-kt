package org.ksharp.typesystem.types

import org.ksharp.typesystem.TypeItemBuilder
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.serializer.TypeSerializer
import org.ksharp.typesystem.serializer.TypeSerializers
import org.ksharp.typesystem.substitution.Substitution
import org.ksharp.typesystem.substitution.Substitutions
import org.ksharp.typesystem.unification.TypeUnification
import org.ksharp.typesystem.unification.TypeUnifications

data class TupleType internal constructor(
    override val attributes: Set<Attribute>,
    val elements: List<Type>,
) : Type {
    override val serializer: TypeSerializer
        get() = TypeSerializers.TupleType

    override val unification: TypeUnification
        get() = TypeUnifications.Tuple

    override val substitution: Substitution
        get() = Substitutions.Tuple

    override val terms: Sequence<Type>
        get() = elements.asSequence()

    override val compound: Boolean = true
    override fun toString(): String = elements.asSequence().map { it.representation }.joinToString(", ")
}

fun TypeItemBuilder.tupleType(factory: ParametricTypeFactoryBuilder) =
    ParametricTypeFactory(this).apply(factory).build().map {
        TupleType(attributes, it)
    }
