package org.ksharp.module

import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.Type

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
    val impls: Set<Impl>,
)
