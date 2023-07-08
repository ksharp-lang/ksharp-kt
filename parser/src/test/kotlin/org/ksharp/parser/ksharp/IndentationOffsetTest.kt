package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

class IndentationOffsetTest : StringSpec({
    "Check add new offsets" {
        IndentationOffset()
            .apply {
                add(0).shouldBeTrue()
                add(5).shouldBeTrue()
                add(4).shouldBeFalse()
                add(5).shouldBeFalse()
                add(10).shouldBeTrue()
            }
    }
    "Check update offsets" {
        IndentationOffset()
            .apply {
                add(0).shouldBeTrue()
                update(5).shouldBe(OffsetAction.SAME)
                update(5).shouldBe(OffsetAction.SAME)
                update(10).shouldBe(OffsetAction.INVALID)
                update(4).shouldBe(OffsetAction.END)
            }
    }
    "Nested offsets update" {
        IndentationOffset()
            .apply {
                add(0).shouldBeTrue()
                add(5).shouldBeTrue()
                update(5).shouldBe(OffsetAction.SAME)
                update(5).shouldBe(OffsetAction.SAME)
                update(4).shouldBe(OffsetAction.PREVIOUS)
                update(0).shouldBe(OffsetAction.END)
            }
    }
})
