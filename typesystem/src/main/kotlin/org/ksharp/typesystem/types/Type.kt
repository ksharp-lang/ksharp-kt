package org.ksharp.typesystem.types

import org.ksharp.common.Either
import org.ksharp.common.ErrorOrValue
import org.ksharp.common.new
import org.ksharp.typesystem.TypeItemBuilder
import org.ksharp.typesystem.TypeSystemBuilder
import org.ksharp.typesystem.TypeSystemErrorCode

sealed interface Type {
    val compound: Boolean get() = true
    val representation: String get() = toString().let { s -> if (compound) "($s)" else s }
}

sealed interface TypeVariable : Type {
    override val compound: Boolean get() = false
}

data class Concrete internal constructor(
    val name: String,
) : TypeVariable {
    override fun toString(): String = name
}

fun TypeItemBuilder.type(name: String): ErrorOrValue<TypeVariable> =
    Either.Right(Concrete(name)).also {
        validation {
            if (it(name) == null)
                TypeSystemErrorCode.TypeNotFound.new("type" to name)
            else null
        }
    }

fun TypeSystemBuilder.type(name: String) =
    item(name) {
        Either.Right(Concrete(name))
    }
