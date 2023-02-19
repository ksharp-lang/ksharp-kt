package org.ksharp.typesystem.types

import org.ksharp.common.*
import org.ksharp.typesystem.TypeItemBuilder
import org.ksharp.typesystem.TypeSystemErrorCode
import org.ksharp.typesystem.validateTypeName

typealias IntersectionTypeFactoryBuilder = IntersectionTypeFactory.() -> Unit

data class IntersectionType internal constructor(
    val params: List<Concrete>
) : Type {
    override val terms: Sequence<Type>
        get() = params.asSequence()

    override fun toString(): String = params.joinToString(" & ") { it.representation }
}

class IntersectionTypeFactory() {
    private var result: ErrorOrValue<ListBuilder<Concrete>> = Either.Right(listBuilder())

    fun type(name: String) {
        result = result.flatMap { params ->
            validateTypeName(name).map {
                params.add(Concrete(it))
                params
            }
        }
    }

    internal fun build() = result.map { it.build() }
}

fun TypeItemBuilder.intersectionType(factory: IntersectionTypeFactoryBuilder) =
    IntersectionTypeFactory().apply(factory).build().map { types ->
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
        IntersectionType(types)
    }