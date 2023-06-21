package org.ksharp.module

import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.types.Type

data class FunctionInfo(
    val attributes: Set<Attribute>,
    val name: String,
    val types: List<Type>
)
