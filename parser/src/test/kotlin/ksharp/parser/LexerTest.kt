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
        }.asSequence().toList().shouldBe(
            listOf(
                LexerToken(
                    type = BaseTokenType.Unknown,
                    token = TextToken("H", 0, 0),
                ),
                LexerToken(
                    type = BaseTokenType.Unknown,
                    token = TextToken("e", 1, 1),
                ),
                LexerToken(
                    type = BaseTokenType.Unknown,
                    token = TextToken(" ", 2, 2)
                ),
                LexerToken(
                    type = BaseTokenType.Unknown,
                    token = TextToken("M", 3, 3)
                ),
                LexerToken(
                    type = BaseTokenType.Unknown,
                    token = TextToken("a", 4, 4)
                ),
                LexerToken(
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
        }.asSequence().toList().shouldBe(
            listOf(
                LexerToken(
                    type = WordToken.Word,
                    token = TextToken("He", 0, 1)
                ),
                LexerToken(
                    type = BaseTokenType.Unknown,
                    token = TextToken(" ", 2, 2)
                ),
                LexerToken(
                    type = WordToken.Word,
                    token = TextToken("man", 3, 5)
                ),
            )
        )
    }

    "Given a lexer, calling collapse should return just three tokens" {
        "He --- man".lexer {
            if (it.isLetter()) {
                consumeWord()
            } else null
        }.collapseTokens()
            .asSequence().toList().shouldBe(
                listOf(
                    LexerToken(
                        type = WordToken.Word,
                        token = TextToken("He", 0, 1)
                    ),
                    LexerToken(
                        type = BaseTokenType.Unknown,
                        token = TextToken(" --- ", 2, 6)
                    ),
                    LexerToken(
                        type = WordToken.Word,
                        token = TextToken("man", 7, 9)
                    ),
                )
            )
    }
})