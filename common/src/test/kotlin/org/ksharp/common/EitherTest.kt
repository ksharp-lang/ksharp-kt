package org.ksharp.common

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class EitherTest : StringSpec({
    "Map a right Either" {
        Either.Right("Hello")
            .map { it.length }
            .apply {
                shouldBe(Either.Right(5))
                isLeft.shouldBe(false)
                isRight.shouldBe(true)
            }
    }
    "Map a left Either" {
        (Either.Left("Hello")
                as Either<String, String>)
            .map { it.length }
            .apply {
                shouldBe(Either.Left("Hello"))
                isLeft.shouldBe(true)
                isRight.shouldBe(false)
            }
    }
    "Unwrap with a left Either" {
        listOf<Either<Boolean, String>>(
            Either.Right("He"),
            Either.Right("llo"),
            Either.Left(false)
        ).unwrap().shouldBe(Either.Left(false))
    }
    "Unwrap with Either" {
        listOf<Either<Boolean, String>>(
            Either.Right("He"),
            Either.Right("llo"),
        ).unwrap().shouldBe(Either.Right(listOf("He", "llo")))
    }
    "Unwrap and transform with a left Either" {
        listOf<Either<Boolean, String>>(
            Either.Right("He"),
            Either.Right("llo"),
            Either.Left(false)
        ).transformAndUnwrap {
            it.length
        }.shouldBe(Either.Left(false))
    }
    "Unwrap and transform with Either" {
        listOf<Either<Boolean, String>>(
            Either.Right("He"),
            Either.Right("llo"),
        ).transformAndUnwrap {
            it.length
        }.shouldBe(Either.Right(listOf(2, 3)))
    }
})