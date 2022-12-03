package org.ksharp.common

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ValidatorTest : StringSpec({
    "Test lazy seq with some not null values" {
        lazySeqBuilder<String>().apply {
            add { "Hello" }
            add { null }
            add { "World" }
            value.joinToString(" ").shouldBe("Hello World")
        }
    }
    "Test lazy seq with just null values" {
        lazySeqBuilder<String>().apply {
            add { null }
            add { null }
            value.joinToString("").shouldBe("")
        }
    }
})