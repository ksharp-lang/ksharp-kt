package org.ksharp.module

import org.ksharp.typesystem.TypeSystem


data class ModuleInfo(
    val dependencies: List<String>,
    val typeSystem: TypeSystem,
    val functions: Map<String, FunctionInfo>
)
