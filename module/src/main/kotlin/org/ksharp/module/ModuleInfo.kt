package org.ksharp.module

import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.Type

data class Impl(
    val trait: String,
    val type: Type
)

data class ModuleInfo(
    // [import-key]: module-name
    val dependencies: Map<String, String>,
    val typeSystem: TypeSystem,
    val functions: Map<String, FunctionInfo>,
    val traits: Map<String, TraitInfo>,
    val impls: Set<Impl>,
)
