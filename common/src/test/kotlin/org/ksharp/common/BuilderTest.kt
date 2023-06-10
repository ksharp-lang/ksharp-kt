package org.ksharp.common

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ListAccumulatorStateTest : StringSpec({
    "Test list builder" {
        listBuilder<String>().apply {
            add("Hello")
            add("World")
            addAll(listOf("!", "@"))
            size().shouldBe(4)
            build().shouldBe(listOf("Hello", "World", "!", "@"))
            size().shouldBe(0)
        }
    }
    "Test map builder" {
        mapBuilder<String, String>().apply {
            put("Key1", "Value1")
            get("Key1").shouldBe("Value1")
            containsKey("Key1").shouldBe(true)
            view.apply {
                get("Key1").shouldBe("Value1")
                containsKey("Key1").shouldBe(true)
            }
            build().shouldBe(mapOf("Key1" to "Value1"))
        }
    }
})
