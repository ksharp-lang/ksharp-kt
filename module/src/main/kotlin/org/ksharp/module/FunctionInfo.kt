package org.ksharp.module

import org.ksharp.typesystem.annotations.Annotation
import org.ksharp.typesystem.types.Type

enum class FunctionVisibility {
    Internal,
    Public
}

data class FunctionInfo(
    val native: Boolean,
    val visibility: FunctionVisibility,
    val annotations: List<Annotation>?,
    val name: String,
    val types: List<Type>
)
