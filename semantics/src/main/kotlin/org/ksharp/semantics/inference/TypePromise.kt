package org.ksharp.semantics.inference

import org.ksharp.typesystem.types.Parameter
import org.ksharp.typesystem.types.Type

interface TypePromise

data class ResolvedTypePromise(
    val type: Type
) : TypePromise

class MaybePolymorphicTypePromise(
    val name: String,
    paramName: String,
) : TypePromise {
    var type: Type = Parameter(paramName)
        private set

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MaybePolymorphicTypePromise

        if (name != other.name) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    override fun toString(): String {
        return "MaybePolymorphicTypePromise(name='$name', type=$type)"
    }

}