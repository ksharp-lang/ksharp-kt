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
})