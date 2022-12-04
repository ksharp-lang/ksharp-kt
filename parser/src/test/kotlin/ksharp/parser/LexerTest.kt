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
                LexerToken(
                    type = BaseTokenType.Unknown,
                    text = TextToken("h", 0, 0),
                ),
                LexerToken(
                    type = BaseTokenType.Unknown,
                    text = TextToken("e", 1, 1),
                ),
                LexerToken(
                    type = BaseTokenType.Unknown,
                    text = TextToken(" ", 2, 2)
                ),
                LexerToken(
                    type = BaseTokenType.Unknown,
                    text = TextToken("M", 3, 3)
                ),
                LexerToken(
                    type = BaseTokenType.Unknown,
                    text = TextToken("a", 4, 4)
                ),
                LexerToken(
                    type = BaseTokenType.Unknown,
                    text = TextToken("n", 5, 5)
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
                LexerToken(
                    type = WordToken.Word,
                    text = TextToken("He", 0, 1)
                ),
                LexerToken(
                    type = BaseTokenType.Unknown,
                    text = TextToken(" ", 2, 2)
                ),
                LexerToken(
                    type = WordToken.Word,
                    text = TextToken("man", 3, 5)
                ),
            )
        )
    }
})