package ksharp.test

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.ksharp.common.Either

fun <L, R> Either<L, R>.shouldBeLeft() = this.shouldBeInstanceOf<Either.Left<*>>()

fun <L, R> Either<L, R>.shouldBeRight() = this.shouldBeInstanceOf<Either.Right<*>>()

fun <L, R> Either<L, R>.shouldBeLeft(expected: L) = this.shouldBeLeft().shouldBe(Either.Left(expected))

fun <L, R> Either<L, R>.shouldBeRight(expected: R) = this.shouldBeRight().shouldBe(Either.Right(expected))