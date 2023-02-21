package org.ksharp.typesystem

import org.ksharp.common.*
import org.ksharp.typesystem.types.Type

typealias PartialTypeSystem = PartialBuilderResult<TypeSystem>

class TypeSystem(
    private val parent: TypeSystem?,
    private val types: Map<String, Type>
) {
    val size: Int = types.size
    operator fun get(name: String): ErrorOrType =
        types[name]?.let { Either.Right(it) }
            ?: parent?.get(name)
            ?: Either.Left(
                TypeSystemErrorCode.TypeNotFound.new(
                    "type" to name
                )
            )
}

val PartialTypeSystem.size get() = value.size
operator fun PartialTypeSystem.get(name: String) = value[name]

fun typeSystem(parent: PartialTypeSystem? = null, block: TypeSystemBuilder.() -> Unit): PartialTypeSystem =
    TypeSystemBuilder(
        parent?.value,
        store = mapBuilder(),
        builder = partialBuilder {
            TypeSystem(parent?.value, it.toMap())
        }
    ).apply(block)
        .build().let {
            if (parent?.isPartial == true) {
                PartialTypeSystem(
                    it.value,
                    parent.errors + it.errors
                )
            } else it
        }
