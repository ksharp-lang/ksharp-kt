package org.ksharp.module

import org.ksharp.typesystem.TypeSystem

data class Impl(
    val trait: String,
    val type: String
)

data class ModuleInfo(
    val dependencies: List<String>,
    val typeSystem: TypeSystem,
    val functions: Map<String, FunctionInfo>,
    val traits: Map<String, TraitInfo>,
    val impls: Set<Impl>,
)
