package org.ksharp.lsp.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.ksharp.lsp.languages.kSharpLanguageId

class DocumentStorageTest : StringSpec({
    "Add and update document" {
        val storage = DocumentStorage()
        storage.add("doc", kSharpLanguageId, "type Num \n\nsum a = a * 2\n   1\n")
        storage.update("doc", sequenceOf(DocumentChange(Range(0 to 0, 4 to 0), "a𐐀b"))).shouldBeTrue()
        storage.content("doc").shouldBe("a𐐀b")
        storage.remove("doc").shouldBe("a𐐀b")
    }
})
