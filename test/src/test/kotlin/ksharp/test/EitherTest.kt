package ksharp.test

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Either

class EitherTest : StringSpec({
    "ShouldBe Left" {
        Either.Left("Left Value")
            .shouldBeLeft()
            .shouldBeLeft("Left Value")
    }
    "ShouldBe Right" {
        Either.Right("Right Value")
            .shouldBeRight()
            .shouldBeRight("Right Value")
    }
})