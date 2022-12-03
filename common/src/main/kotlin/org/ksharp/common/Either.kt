package org.ksharp.common

sealed class Either<out L, out R> {
    data class Left<out V>(val value: V) : Either<V, Nothing>()
    data class Right<out V>(val value: V) : Either<Nothing, V>()

    private fun <V> fold(whenLeft: (L) -> V, whenRight: (R) -> V): V = when (this) {
        is Right -> whenRight(this.value)
        is Left -> whenLeft(this.value)
    }

    fun <Rp> map(f: (R) -> Rp): Either<L, Rp> =
        fold({ Left(it) }, { Right(f(it)) })

    fun <Lp> mapLeft(f: (L) -> Lp): Either<Lp, R> =
        fold({ Left(f(it)) }, { Right(it) })

    @Suppress("UNCHECKED_CAST")
    fun <Lp, Rp> flatMap(f: (R) -> Either<Lp, Rp>): Either<Lp, Rp> = when (this) {
        is Right -> f(value)
        is Left -> this as Either<Lp, Nothing>
    }
    
}

val Either<*, *>.isRight: Boolean
    get() = when (this) {
        is Either.Right -> true
        is Either.Left -> false
    }

val Either<*, *>.isLeft: Boolean
    get() = !isRight

