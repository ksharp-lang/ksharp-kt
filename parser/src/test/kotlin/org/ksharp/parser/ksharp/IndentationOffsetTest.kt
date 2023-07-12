package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

class IndentationOffsetTest : StringSpec({
    "Check add new offsets" {
        IndentationOffset()
            .apply {
                add(0, OffsetType.Normal).shouldBeTrue()
                add(5, OffsetType.Normal).shouldBeTrue()
                add(4, OffsetType.Normal).shouldBeFalse()
                add(5, OffsetType.Normal).shouldBeTrue()
                add(10, OffsetType.Normal).shouldBeTrue()
            }
    }
    "Check update offsets" {
        IndentationOffset()
            .apply {
                add(0, OffsetType.Normal).shouldBeTrue()
                update(5).shouldBe(OffsetAction.Same)
                update(5).shouldBe(OffsetAction.Same)
                update(10).shouldBe(OffsetAction.Invalid)
                update(4).shouldBe(OffsetAction.End)
            }
    }
    "Nested offsets update" {
        IndentationOffset()
            .apply {
                add(0, OffsetType.Normal).shouldBeTrue()
                add(5, OffsetType.Normal).shouldBeTrue()
                update(5).shouldBe(OffsetAction.Same)
                update(5).shouldBe(OffsetAction.Same)
                update(4).shouldBe(OffsetAction.Previous)
                update(0).shouldBe(OffsetAction.End)
            }
    }
})
