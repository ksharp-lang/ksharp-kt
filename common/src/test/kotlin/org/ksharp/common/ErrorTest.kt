package org.ksharp.common

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

private class ErrorExample(
    override val name: String = "EX001",
    override val description: String = "ExampleError {Argument1}"
) :
    ErrorCode

private val location = Location.NoProvided

class ErrorTest : StringSpec({
    "Test error creation with arguments" {
        ErrorExample().new("Argument1" to "Test Replace").apply {
            location.shouldBeNull()
            toString().shouldBe("EX001: ExampleError Test Replace")
        }

    }
    "Test error creation no passing arguments" {
        ErrorExample().new().apply {
            location.shouldBeNull()
            toString().shouldBe("EX001: ExampleError {Argument1}")
        }
    }
    "Test error creation with location" {
        ErrorExample().new(location, "Argument1" to "Test Replace").apply {
            location.shouldBe(
                Location.NoProvided
            )
            toString().shouldBe("EX001: ExampleError Test Replace")
        }
    }
    "Test error creation with location no passing arguments" {
        ErrorExample().new(location).apply {
            location.shouldBe(
                Location.NoProvided
            )
            toString().shouldBe("EX001: ExampleError {Argument1}")
        }
    }
})
