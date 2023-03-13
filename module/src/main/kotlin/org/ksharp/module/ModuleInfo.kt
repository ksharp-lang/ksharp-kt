package org.ksharp.module

import org.ksharp.common.mapBuilder
import org.ksharp.common.put
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.Type

data class ModuleInfo(
    val typeSystem: TypeSystem,
    val functions: Map<String, FunctionInfo>
)

class ModuleInfoBuilder {
    private var functions = mapBuilder<String, FunctionInfo>()

    fun add(name: String, vararg types: Type) {
        functions.put(
            name, FunctionInfo(
                name,
                types.toList()
            )
        )
    }

    internal fun build() = functions.build()
}

fun moduleFunctions(body: ModuleInfoBuilder.() -> Unit) =
    ModuleInfoBuilder().apply(body).build()