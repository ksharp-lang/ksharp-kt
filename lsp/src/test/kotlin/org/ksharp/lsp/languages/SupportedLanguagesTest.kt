package org.ksharp.lsp.languages

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SupportedLanguagesTest : StringSpec({
    "Assert ksharp language filter" {
        kSharpLanguageId.shouldBe("ksharp")
        kSharpDocumentSelector.language.shouldBe(kSharpLanguageId)
    }

    "Assert supported languages" {
        supportedLanguages.shouldBe(listOf(kSharpDocumentSelector))
    }
})
