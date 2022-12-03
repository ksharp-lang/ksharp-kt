package org.ksharp.common

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

enum class PartialBuilderErrors(override val description: String) :
    ErrorCode {
    Error1("Error1"),
}

class BuilderTest : StringSpec({
    "Builder with errors" {
        partialBuilder<String, String> {
            it.joinToString(" ")
        }.apply {
            item {
                value("Hello")
                validation { PartialBuilderErrors.Error1.new() }
            }
            build().apply {
                shouldBe(PartialBuilderResult("", listOf(PartialBuilderErrors.Error1.new())))
                isPartial.shouldBe(true)
            }
        }
    }
    "Builder successful" {
        partialBuilder<String, String> {
            it.joinToString(" ")
        }.apply {
            item {
                value("Hello")
            }
            item {
                value("World")
                validation { PartialBuilderErrors.Error1.new() }
            }
            build().apply {
                shouldBe(PartialBuilderResult("Hello", listOf(PartialBuilderErrors.Error1.new())))
                isPartial.shouldBe(true)
            }
        }
    }
    "Builder successful with mapping" {
        partialBuilder<String, Int> {
            it.count()
        }.apply {
            item { value("Hello") }
            build().apply {
                shouldBe(PartialBuilderResult(1, listOf()))
                isPartial.shouldBe(false)
            }
        }
    }
})