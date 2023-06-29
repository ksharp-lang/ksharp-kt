package org.ksharp.module

import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.types.Type

interface FunctionInfo {
    val attributes: Set<Attribute>
    val name: String
    val types: List<Type>
}

internal data class FunctionInfoImpl(
    override val attributes: Set<Attribute>,
    override val name: String,
    override val types: List<Type>
) : FunctionInfo

fun functionInfo(attributes: Set<Attribute>, name: String, types: List<Type>): FunctionInfo =
    FunctionInfoImpl(attributes, name, types)
