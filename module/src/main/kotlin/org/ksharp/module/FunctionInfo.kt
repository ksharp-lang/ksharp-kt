package org.ksharp.module

import org.ksharp.typesystem.types.Type

data class FunctionInfo(
    val dependency: String?,
    val name: String,
    val types: List<Type>
)