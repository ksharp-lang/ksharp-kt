package org.ksharp.typesystem.types

import org.ksharp.common.*
import org.ksharp.typesystem.TypeItemBuilder
import org.ksharp.typesystem.TypeSystemErrorCode
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.reducer.Reducer
import org.ksharp.typesystem.reducer.Reducers
import org.ksharp.typesystem.serializer.TypeSerializer
import org.ksharp.typesystem.serializer.TypeSerializers
import org.ksharp.typesystem.substitution.Substitution
import org.ksharp.typesystem.substitution.Substitutions
import org.ksharp.typesystem.unification.TypeUnification
import org.ksharp.typesystem.unification.TypeUnifications
import org.ksharp.typesystem.validateTypeName

typealias IntersectionTypeFactoryBuilder = IntersectionTypeFactory.() -> Unit

data class IntersectionType internal constructor(
    override val attributes: Set<Attribute>,
    val params: List<Type>
) : Type {
    override val reducer: Reducer
        get() = Reducers.NoDefined
    override val serializer: TypeSerializer
        get() = TypeSerializers.IntersectionType

    override val unification: TypeUnification
        get() = TypeUnifications.NoDefined

    override val substitution: Substitution
        get() = Substitutions.Intersection

    override val terms: Sequence<Type>
        get() = params.asSequence()

    override fun toString(): String = params.joinToString(" & ") { it.representation }

    override fun new(attributes: Set<Attribute>): Type = IntersectionType(attributes, params)
}

class IntersectionTypeFactory(
    val attributes: Set<Attribute>
) {
    private var result: ErrorOrValue<ListBuilder<Alias>> = Either.Right(listBuilder())

    fun type(name: String) {
        result = result.flatMap { params ->
            validateTypeName(name).map {
                params.add(Alias(it))
                params
            }
        }
    }

    fun error(error: Error) {
        result = Either.Left(error)
    }

    internal fun build() = result.map { it.build() }
}

fun TypeItemBuilder.intersectionType(factory: IntersectionTypeFactoryBuilder) =
    IntersectionTypeFactory(attributes).apply(factory).build().map { types ->
        types.forEach { tp ->
            validation {
                val type = it(tp.name)
                if (type !is IsTrait) {
                    TypeSystemErrorCode.IntersectionTypeShouldBeTraits.new(
                        "name" to tp.name
                    )
                } else null
            }
        }
        IntersectionType(attributes, types)
    }
