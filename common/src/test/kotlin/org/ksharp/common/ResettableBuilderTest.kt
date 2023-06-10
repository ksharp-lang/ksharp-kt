package org.ksharp.common

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class ResettableBuilderTest : StringSpec({
    "Resettable List Builder Test" {
        val builder = resettableListBuilder<String>()
        builder.update {
            it.add("Hello")
        }
        builder.update {
            it.add("World")
        }
        builder.build().shouldBe(listOf("Hello", "World"))
    }
    "Resettable List Builder Empty" {
        val builder = resettableListBuilder<String>()
        builder.update {}
        builder.build().shouldBeEmpty()
    }
    "Resettable List Builder Null" {
        val builder = resettableListBuilder<String>()
        builder.build().shouldBeNull()
    }
    "Resettable value" {
        val value = resettableValue<Int>()
        value.apply {
            get().shouldBeNull()
            set(10)
            get().shouldBe(10)
            get().shouldBeNull()
            set(20)
            reset()
            get().shouldBeNull()
        }
    }
})
