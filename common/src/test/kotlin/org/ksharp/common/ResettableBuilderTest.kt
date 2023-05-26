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
        builder.build().shouldBe(listOf("Hello"))
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
})
