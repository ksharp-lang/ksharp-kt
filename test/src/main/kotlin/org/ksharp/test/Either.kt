package org.ksharp.test

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.ksharp.common.Either

fun <L, R> Either<L, R>.shouldBeLeft() = this.shouldBeInstanceOf<Either.Left<L>>()

fun <L, R> Either<L, R>.shouldBeRight() = this.shouldBeInstanceOf<Either.Right<R>>()

fun <L, R> Either<L, R>.shouldBeLeft(expected: L) = this.shouldBeLeft().shouldBe(Either.Left(expected))

fun <L, R> Either<L, R>.shouldBeRight(expected: R) = this.shouldBeRight().shouldBe(Either.Right(expected))