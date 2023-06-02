package org.ksharp.lsp.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class KSharpDocumentTest : StringSpec({
    "Lines and size" {
        val doc = document("type Num \n\nsum a = a * 2\n   1\n")
        doc.lines.shouldBe(5)
        doc.line(0).shouldBe("type Num ")
        doc.line(1).shouldBe("")
        doc.line(2).shouldBe("sum a = a * 2")
        doc.line(3).shouldBe("   1")
        doc.line(4).shouldBe("")
        doc.content.shouldBe("type Num \n\nsum a = a * 2\n   1\n")
        doc.length.shouldBe(30)
    }
    "Replace all content" {
        val doc = document("type Num \n\nsum a = a * 2\n   1\n")
        doc.update(Range(0 to 0, 4 to 0, 30), "a𐐀b")
        doc.asSequence().toList().shouldBe(listOf("a𐐀b"))
    }
    "Replace a line" {
        val doc = document("type Num \n\nsum a = a * 2\n   1\n")
        doc.update(Range(1 to 0, 1 to 0, 0), "abc")
        doc.content.shouldBe("type Num \nabc\nsum a = a * 2\n   1\n")
        doc.asSequence()
            .toList().shouldBe(
                listOf(
                    "type Num ",
                    "abc",
                    "sum a = a * 2",
                    "   1",
                    ""
                )
            )
    }
    "Add content at the end" {
        val doc = document("type Num \n\nsum a = a * 2\n   1\n")
        doc.update(Range(4 to 0, 4 to 0, 0), "\n   ")
        doc.content.shouldBe("type Num \n\nsum a = a * 2\n   1\n\n   ")
        doc.asSequence()
            .toList().shouldBe(
                listOf(
                    "type Num ",
                    "",
                    "sum a = a * 2",
                    "   1",
                    "",
                    "   "
                )
            )
    }
    "Add a new char in a line" {
        val doc = document("type Num \n\nsum a = a * 2\n   1\n")
        doc.update(Range(2 to 8, 2 to 8, 0), "b")
        doc.content.shouldBe("type Num \n\nsum a = ba * 2\n   1\n")
        doc.asSequence()
            .toList().shouldBe(
                listOf(
                    "type Num ",
                    "",
                    "sum a = ba * 2",
                    "   1",
                    ""
                )
            )
    }
    "Add two lines in the middle" {
        val doc = document("type Num \n\nsum a = a * 2\n   1\n")
        doc.update(Range(2 to 12, 2 to 13, 1), "ab\ncd")
        doc.content.shouldBe("type Num \n\nsum a = a * ab\ncd\n   1\n")
        doc.asSequence()
            .toList().shouldBe(
                listOf(
                    "type Num ",
                    "",
                    "sum a = a * ab",
                    "cd",
                    "   1",
                    ""
                )
            )
    }
})
