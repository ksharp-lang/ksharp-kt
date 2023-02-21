package org.ksharp.typesystem

import org.ksharp.common.*

class ModuleTypeSystem(
    val parent: TypeSystem?,
    val imports: Map<String, TypeSystem>
) : TypeSystem {
    override val size: Int
        get() = 0

    override fun get(name: String): ErrorOrType {
        val ix = name.indexOf('.')
        return (if (ix != -1) {
            val key = name.substring(0, ix)
            val type = name.substring(ix + 1)
            imports[key]?.let {
                it[type]
            }
        } else parent?.get(name)) ?: Either.Left(
            TypeSystemErrorCode.TypeNotFound.new(
                "type" to name
            )
        )
    }

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