package org.ksharp.lsp.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.MarkupKind
import org.eclipse.lsp4j.Position
import org.ksharp.lsp.actions.AbstractionsHoverAction
import org.ksharp.lsp.languages.kSharpLanguageId

class DocumentStorageTest : StringSpec({
    "Add and update document" {
        val storage = DocumentStorage()
        storage.add("doc", kSharpLanguageId, "type Num \n\nsum a = a * 2\n   1\n")
        storage.update("doc", sequenceOf(DocumentChange(Range(0 to 0, 4 to 0), "a𐐀b"))).shouldBeTrue()
        storage.content("doc").shouldBe("a𐐀b")
        storage.remove("doc").shouldBe("a𐐀b")
        storage.remove("doc").shouldBeNull()
    }
    "Remove not existent document" {
        val storage = DocumentStorage()
        storage.remove("doc").shouldBeNull()
    }
    "Update content over not existent document" {
        val storage = DocumentStorage()
        storage.update("doc", sequenceOf(DocumentChange(Range(0 to 0, 4 to 0), "a𐐀b"))).shouldBeFalse()
        storage.content("doc").shouldBeNull()
    }
    "Calculate Hover" {
        val storage = DocumentStorage()
        storage.add("doc", kSharpLanguageId, "sum a b = a + b")
        storage.executeAction("doc", AbstractionsHoverAction, Position().apply {
            line = 0
            character = 1
        })
            .get()
            .shouldBe(
                Hover().apply {
                    setContents(
                        MarkupContent(
                            MarkupKind.PLAINTEXT,
                            "sum/2 :: ((Num a) -> (Num a) -> (Num a))"
                        )
                    )
                }
            )
    }
})
