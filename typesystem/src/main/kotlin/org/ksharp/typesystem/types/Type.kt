package org.ksharp.typesystem.types

import org.ksharp.common.Either
import org.ksharp.common.ErrorOrValue
import org.ksharp.common.new
import org.ksharp.typesystem.*
import org.ksharp.typesystem.annotations.Annotation

interface Type {
    val compound: Boolean get() = true
    val terms: Sequence<Type>
    val representation: String get() = toString().let { s -> if (compound) "($s)" else s }
}

sealed interface TypeVariable : Type {
    override val compound: Boolean get() = false
    override val terms: Sequence<Type> get() = emptySequence()
}

data class Concrete internal constructor(
    val name: String
) : TypeVariable {
    override fun toString(): String = name
}


data class Alias internal constructor(
    val name: String
) : TypeVariable {
    override fun toString(): String {
        return "@$name"
    }
}

fun TypeSystem.alias(name: String): ErrorOrType =
    this[name].map {
        Concrete(name)
    }


fun TypeItemBuilder.type(name: String): ErrorOrValue<TypeVariable> =
    Either.Right(Concrete(name)).also {
        validation {
            if (it(name) == null)
                TypeSystemErrorCode.TypeNotFound.new("type" to name)
            else null
        }
    }

fun TypeSystemBuilder.type(name: String, annotations: List<Annotation> = listOf()) =
    item(name, annotations) {
        Either.Right(Concrete(name))
    }
