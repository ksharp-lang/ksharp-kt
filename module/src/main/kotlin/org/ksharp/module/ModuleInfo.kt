package org.ksharp.module

import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.types.Type

data class Impl(
    val attributes: Set<Attribute>,
    val trait: String,
    val type: Type
)

data class ModuleInfo(
    // [import-key]: module-name
    val dependencies: Map<String, String>,
    val typeSystem: TypeSystem,
    val functions: Map<String, FunctionInfo>,
    val impls: Set<Impl>,
)
