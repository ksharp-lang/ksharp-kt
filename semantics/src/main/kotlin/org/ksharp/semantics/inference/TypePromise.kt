package org.ksharp.semantics.inference

import org.ksharp.common.Either
import org.ksharp.common.Error
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.Type

interface TypePromise

data class ErrorTypePromise(
    val error: Error
) : TypePromise

data class ResolvedTypePromise(
    val type: Type
) : TypePromise

data class MaybePolymorphicTypePromise(
    val name: String
) : TypePromise

fun TypeSystem.getTypePromise(name: String) =
    get(name).let {
        when (it) {
            is Either.Left -> ErrorTypePromise(it.value)
            is Either.Right -> ResolvedTypePromise(it.value)
        }
    }