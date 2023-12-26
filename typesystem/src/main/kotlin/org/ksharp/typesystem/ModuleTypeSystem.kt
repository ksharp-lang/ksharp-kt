package org.ksharp.typesystem

import org.ksharp.common.*
import org.ksharp.typesystem.types.Type

class ModuleTypeSystem(
    override val handle: HandlePromise<TypeSystem>,
    override val parent: TypeSystem?,
    private val imports: Map<String, TypeSystem>
) : TypeSystem {

    private fun typeSystemAndTypeName(name: String): Pair<TypeSystem?, String> {
        val ix = name.indexOf('.')
        return if (ix != -1) {
            imports[name.substring(0, ix)] to name.substring(ix + 1)
        } else parent to name
    }

    private fun lookup(name: String): Either<Error, Pair<String, TypeSystem>> {
        val (typeSystem, type) = typeSystemAndTypeName(name)
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

    override fun has(name: String): Boolean {
        val (typeSystem, type) = typeSystemAndTypeName(name)
        return typeSystem?.has(type) == true
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

    fun register(key: String, typeSystem: TypeSystem) {
        builder.put(key, typeSystem)
    }

    internal fun build() =
        handlePromise<TypeSystem>().let { handle ->
            PartialTypeSystem(
                ModuleTypeSystem(handle, parent, builder.build()).also {
                    handle.set(it)
                },
                errors
            )
        }
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
