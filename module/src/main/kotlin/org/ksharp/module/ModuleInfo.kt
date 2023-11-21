package org.ksharp.module

import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.Type

interface ModuleInterface {

    val impls: Set<Impl>

    fun type(name: String): ErrorOrType

    fun function(name: String): FunctionInfo?

}

data class Impl(
    val trait: String,
    val type: Type
)

data class ModuleInfo(
    /**
     * Map module dependencies the structure of the map is
     * {key: path}
     */
    val dependencies: Map<String, String>,
    val typeSystem: TypeSystem,
    val functions: Map<String, FunctionInfo>,
    override val impls: Set<Impl>,
) : ModuleInterface {
    override fun type(name: String): ErrorOrType = typeSystem[name]

    override fun function(name: String): FunctionInfo? = functions[name]

}
