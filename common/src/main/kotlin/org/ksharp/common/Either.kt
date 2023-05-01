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

    val valueOrNull
        get(): R? =
            when (this) {
                is Right -> this.value
                is Left -> null
            }
}

val Either<*, *>.isRight: Boolean
    get() = when (this) {
        is Either.Right -> true
        is Either.Left -> false
    }

val Either<*, *>.isLeft: Boolean
    get() = !isRight


fun <L, R> Sequence<Either<L, R>>.unwrap(): Either<L, List<R>> {
    val result = listBuilder<R>()
    return onEach {
        if (it.isRight) {
            result.add(it.cast<Either.Right<R>>().value)
        }
    }.firstOrNull { it.isLeft }?.cast<Either.Left<L>>()
        ?: Either.Right(result.build())
}

fun <L, R, NR> Sequence<Either<L, R>>.transformAndUnwrap(transform: (r: R) -> NR): Either<L, List<NR>> {
    val result = listBuilder<NR>()
    return onEach {
        if (it.isRight) {
            result.add(transform(it.cast<Either.Right<R>>().value))
        }
    }.firstOrNull { it.isLeft }?.cast<Either.Left<L>>()
        ?: Either.Right(result.build())
}

fun <L, R> List<Either<L, R>>.unwrap(): Either<L, List<R>> =
    asSequence().unwrap()

fun <L, R, NR> List<Either<L, R>>.transformAndUnwrap(transform: (r: R) -> NR): Either<L, List<NR>> =
    asSequence().transformAndUnwrap(transform)