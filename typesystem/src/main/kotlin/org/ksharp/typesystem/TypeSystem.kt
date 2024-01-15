package org.ksharp.typesystem

import org.ksharp.common.*
import org.ksharp.typesystem.types.Type

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
}

class TypeSystemImpl internal constructor(
    override val parent: TypeSystem?,
    override val handle: HandlePromise<TypeSystem>,
    private val types: Map<String, Type>
) : TypeSystem {
    override val size: Int = types.size

    override fun asSequence(): Sequence<Pair<String, Type>> =
        types.asSequence().map {
            it.key to it.value
        }.let { types ->
            if (parent != null) {
                sequenceOf(types, parent.asSequence()).flatten()
            } else types
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

fun typeSystem(
    parent: PartialTypeSystem? = null,
    handle: HandlePromise<TypeSystem> = handlePromise(),
    block: TypeSystemBuilder.() -> Unit
): PartialTypeSystem =
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
