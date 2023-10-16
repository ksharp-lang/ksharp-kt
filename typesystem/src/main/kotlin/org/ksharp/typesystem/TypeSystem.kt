package org.ksharp.typesystem

import org.ksharp.common.*
import org.ksharp.typesystem.attributes.merge
import org.ksharp.typesystem.types.*

typealias PartialTypeSystem = PartialBuilderResult<TypeSystem>

interface TypeSystem {
    val size: Int

    val parent: TypeSystem?

    val handle: HandlePromise<TypeSystem>

    fun asSequence(): Sequence<Pair<String, Type>>

    /**
     * return the type value resolved
     */
    operator fun get(name: String): ErrorOrType

    operator fun invoke(type: Type): ErrorOrType =
        when (type) {
            is Alias -> this[type.name].flatMap {
                this(it)
            }

            is Labeled -> this(type.type).map {
                Labeled(type.label, it)
            }

            is TypeAlias -> this[type.name].flatMap {
                this(it)
            }.map { it.new(it.attributes.merge(type.attributes)) }

            is TypeConstructor -> this[type.alias]
            else -> Either.Right(type)
        }
}

class TypeSystemImpl internal constructor(
    override val parent: TypeSystem?,
    override val handle: HandlePromise<TypeSystem>,
    private val types: Map<String, Type>
) : TypeSystem {
    override val size: Int = types.size

    override fun asSequence(): Sequence<Pair<String, Type>> = types.asSequence().map {
        it.key to it.value
    }

    override fun get(name: String): ErrorOrType =
        types[name]?.let { Either.Right(it) }
            ?: parent?.get(name)
            ?: Either.Left(
                TypeSystemErrorCode.TypeNotFound.new(
                    "type" to name
                )
            )

}

val PartialTypeSystem.size get() = value.size

val PartialTypeSystem.handle get() = value.handle
operator fun PartialTypeSystem.get(name: String) = value[name]

operator fun PartialTypeSystem.invoke(type: Type) = value(type)

fun typeSystem(parent: PartialTypeSystem? = null, block: TypeSystemBuilder.() -> Unit): PartialTypeSystem =
    handlePromise<TypeSystem>().let { handle ->
        TypeSystemBuilder(
            parent?.value,
            handle,
            store = mapBuilder(),
            builder = partialBuilder {
                TypeSystemImpl(parent?.value, handle, it.toMap())
            }
        ).apply(block)
            .build().let {
                handle.set(it.value)
                if (parent?.isPartial == true) {
                    PartialTypeSystem(
                        it.value,
                        parent.errors + it.errors
                    )
                } else it
            }
    }
