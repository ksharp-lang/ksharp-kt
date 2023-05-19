package org.ksharp.module

import org.ksharp.typesystem.types.Type

enum class FunctionVisibility {
    Internal,
    Public
}

data class FunctionInfo(
    val visibility: FunctionVisibility,
    val dependency: String?,
    val name: String,
    val types: List<Type>
)