package org.ksharp.module

import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.types.isUnitType

interface FunctionInfo {
    val attributes: Set<Attribute>
    val name: String
    val types: List<Type>
    val arity: Int

    val nameWithArity: String
        get() = "$name/$arity"

}

internal data class FunctionInfoImpl(
    override val attributes: Set<Attribute>,
    override val name: String,
    override val types: List<Type>
) : FunctionInfo {
    override val arity: Int by lazy {
        when (val size = types.size) {
            2 -> if (types.first().isUnitType) 0 else 1
            else -> size - 1
        }
    }
}

fun functionInfo(attributes: Set<Attribute>, name: String, types: List<Type>): FunctionInfo =
    FunctionInfoImpl(attributes, name, types)
