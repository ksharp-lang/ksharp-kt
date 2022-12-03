package org.ksharp.typesystem

import org.ksharp.common.Either
import org.ksharp.common.PartialBuilderResult
import org.ksharp.common.new
import org.ksharp.common.partialBuilder
import org.ksharp.typesystem.types.Type

typealias PartialTypeSystem = PartialBuilderResult<TypeSystem>

class TypeSystem(private val types: Map<String, Type>) {
    val size: Int = types.size
    operator fun get(name: String): ErrorOrType =
        types[name]?.let { Either.Right(it) } ?: Either.Left(
            TypeSystemErrorCode.TypeNotFound.new(
                "type" to name
            )
        )
}

val PartialTypeSystem.size get() = value.size
operator fun PartialTypeSystem.get(name: String) = value[name]

fun typeSystem(block: TypeSystemBuilder.() -> Unit): PartialTypeSystem =
    TypeSystemBuilder(
        store = mutableMapOf(),
        builder = partialBuilder {
            TypeSystem(it.toMap())
        }
    ).apply(block)
        .build()
