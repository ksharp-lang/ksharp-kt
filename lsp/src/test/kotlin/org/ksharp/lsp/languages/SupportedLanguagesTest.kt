package org.ksharp.lsp.languages

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SupportedLanguagesTest : StringSpec({
    "Assert ksharp language filter" {
        kSharpDocumentSelector.language.shouldBe("ksharp")
    }

    "Assert supported languages" {
        supportedLanguages.shouldBe(listOf(kSharpDocumentSelector))
    }
})
