package org.ksharp.semantics.inference

import org.ksharp.common.Either
import org.ksharp.common.Error
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.types.newParameter

sealed interface TypePromise

data class ErrorTypePromise(
    val error: Error
) : TypePromise

data class ResolvedTypePromise(
    val type: Type
) : TypePromise

fun TypeSystem.getTypePromise(name: String) =
    get(name).let {
        when (it) {
            is Either.Left -> ErrorTypePromise(it.value)
            is Either.Right -> ResolvedTypePromise(it.value)
        }
    }

fun paramTypePromise() = ResolvedTypePromise(newParameter())

val TypePromise.type: ErrorOrType
    get() =
        when (this) {
            is ResolvedTypePromise -> Either.Right(this.type)
            is ErrorTypePromise -> Either.Left(this.error)
        }