package org.ksharp.module

import org.ksharp.typesystem.types.Type

data class FunctionInfo(
    val name: String,
    val type: List<Type>
)