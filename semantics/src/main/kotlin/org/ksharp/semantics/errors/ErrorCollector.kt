package org.ksharp.semantics.errors

import org.ksharp.common.*

class ErrorCollector {
    private val errors = listBuilder<Error>()

    fun <T> collect(result: ErrorOrValue<T>): ErrorOrValue<T> =
        when (result) {
            is Either.Left -> {
                errors.add(result.value)
                result
            }

            else -> result
        }

    fun collectAll(errors: List<Error>) {
        this.errors.addAll(errors)
    }

    fun build() = errors.build()
}