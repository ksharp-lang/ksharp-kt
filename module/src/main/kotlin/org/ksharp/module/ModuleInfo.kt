package org.ksharp.module

import org.ksharp.common.*
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.Type


data class ModuleInfo(
    val dependencies: List<String>,
    val typeSystem: TypeSystem,
    val functions: Map<String, List<FunctionInfo>>
)

class ModuleInfoBuilder {
    private var functions = mapBuilder<String, ListBuilder<FunctionInfo>>()

    fun add(name: String, vararg types: Type) {
        add(null, name, *types)
    }

    fun add(dependency: String?, name: String, vararg types: Type) {
        val functionsList = functions.get(name) ?: run {
            val functionList = listBuilder<FunctionInfo>()
            functions.put(name, functionList)
            functionList
        }
        functionsList.add(
            FunctionInfo(
                FunctionVisibility.Public,
                dependency,
                name,
                types.toList()
            )
        )
    }

    internal fun build() = functions.build()
        .asSequence()
        .associate {
            it.key to it.value.build()
        }
}

fun moduleFunctions(body: ModuleInfoBuilder.() -> Unit): Map<String, List<FunctionInfo>> =
    ModuleInfoBuilder().apply(body).build()

