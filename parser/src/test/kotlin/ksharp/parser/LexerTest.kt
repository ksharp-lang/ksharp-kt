package ksharp.parser

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

enum class WordToken : TokenType {
    Word,
}

fun Lexer.consumeWord(): LexerToken {
    while (true) {
        val c = nextChar()
        if (c == null || !c.isLetter()) {
            return token(WordToken.Word, 1)
        }
    }
}

class LexerTest : StringSpec({
    "Given a lexer without rules should return unknown tokens" {
        "He Man".reader().lexer {
            null
        }.toList().shouldBe(
            listOf(
                LToken(
                    type = BaseTokenType.Unknown,
                    token = TextToken("H", 0, 0),
                ),
                LToken(
                    type = BaseTokenType.Unknown,
                    token = TextToken("e", 1, 1),
                ),
                LToken(
                    type = BaseTokenType.Unknown,
                    token = TextToken(" ", 2, 2)
                ),
                LToken(
                    type = BaseTokenType.Unknown,
                    token = TextToken("M", 3, 3)
                ),
                LToken(
                    type = BaseTokenType.Unknown,
                    token = TextToken("a", 4, 4)
                ),
                LToken(
                    type = BaseTokenType.Unknown,
                    token = TextToken("n", 5, 5)
                )
            )
        )
    }
    "Given a lexer with a rule, should return tokens" {
        "He man".lexer {
            if (it.isLetter()) {
                consumeWord()
            } else null
        }.toList().shouldBe(
            listOf(
                LToken(
                    type = WordToken.Word,
                    token = TextToken("He", 0, 1)
                ),
                LToken(
                    type = BaseTokenType.Unknown,
                    token = TextToken(" ", 2, 2)
                ),
                LToken(
                    type = WordToken.Word,
                    token = TextToken("man", 3, 5)
                ),
            )
        )
    }
})