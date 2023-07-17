package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class LineOffsetTest : StringSpec({
    "Add Offsets" {
        val lineOffsets = LineOffset()
        lineOffsets.add(0)
        lineOffsets.add(20)
        lineOffsets.size(25).shouldBe(5)
    }
    "Calculate size empty line offsets" {
        val lineOffsets = LineOffset()
        lineOffsets.size(25).shouldBe(25)
    }
})
