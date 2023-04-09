package org.ksharp.typesystem

import org.ksharp.common.*
import org.ksharp.typesystem.types.Type

class ModuleTypeSystem(
    override val parent: TypeSystem?,
    private val imports: Map<String, TypeSystem>
) : TypeSystem {

    private fun lookup(name: String): Either<Error, Pair<String, TypeSystem>> {
        val ix = name.indexOf('.')
        val (type, typeSystem) = if (ix != -1) {
            val key = name.substring(0, ix)
            val type = name.substring(ix + 1)
            type to imports[key]
        } else name to parent
        return typeSystem?.let { Either.Right(type to it) } ?: Either.Left(
            TypeSystemErrorCode.TypeNotFound.new(
                "type" to name
            )
        )
    }

    override val size: Int
        get() = 0

    override fun get(name: String): ErrorOrType =
        lookup(name).flatMap { (type, typeSystem) ->
            typeSystem[type]
        }

    override fun asSequence(): Sequence<Pair<String, Type>> = emptySequence()
}

class ModuleTypeSystemBuilder(
    val parent: TypeSystem?
) {
    private val builder: MapBuilder<String, TypeSystem> = mapBuilder()
    private var errors: List<Error> = emptyList()

    fun register(key: String, typeSystem: PartialTypeSystem) {
        builder.put(key, typeSystem.value)
        errors = errors + typeSystem.errors
    }

    internal fun build() = PartialTypeSystem(
        ModuleTypeSystem(parent, builder.build()),
        errors
    )
}

fun moduleTypeSystem(parent: PartialTypeSystem? = null, block: ModuleTypeSystemBuilder.() -> Unit): PartialTypeSystem =
    ModuleTypeSystemBuilder(
        parent?.value
    ).apply(block)
        .build().let {
            if (parent?.isPartial == true) {
                PartialTypeSystem(
                    it.value,
                    parent.errors + it.errors
                )
            } else it
        }