package ksharp.parser

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class LexerTokenTest : StringSpec({
    "Assert LexerToken properties access" {
        val token = LexerToken(
            BaseTokenType.Unknown,
            TextToken("H", 2, 3)
        )
        token.type.shouldBe(BaseTokenType.Unknown)
        token.text.shouldBe("H")
        token.startOffset.shouldBe(2)
        token.endOffset.shouldBe(3)
    }
})