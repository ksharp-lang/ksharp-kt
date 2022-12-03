package org.ksharp.common

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ListAccumulatorStateTest : StringSpec({
    "Test list builder" {
        listBuilder<String>().apply {
            add("Hello")
            add("World")
            value.shouldBe(listOf("Hello", "World"))
        }
    }
    "Test map builder" {
        mapBuilder<String, String>().apply {
            add("Key1" to "Value1")
            containsKey("Key1").shouldBe(true)
            value.shouldBe(mapOf("Key1" to "Value1"))
        }
    }
})