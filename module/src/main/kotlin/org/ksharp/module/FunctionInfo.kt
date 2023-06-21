package org.ksharp.module

import org.ksharp.typesystem.types.Type

enum class FunctionVisibility {
    Internal,
    Public
}

data class FunctionInfo(
    val attributes: Set<Attribute>,
    val name: String,
    val types: List<Type>
)
