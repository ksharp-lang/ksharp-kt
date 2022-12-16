package ksharp.parser

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.Line
import org.ksharp.common.Offset

enum class WordToken : TokenType {
    Word, NewLine
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

    "Given a lexer with newLines, convert tokens to logical tokens" {
        "Hello\nWorld\nFS".lexer {
            if (it.isLetter()) {
                consumeWord()
            } else if (it == '\n') {
                token(WordToken.NewLine, 0)
            } else null
        }.toLogicalLexerToken(WordToken.NewLine)
            .asSequence()
            .toList().onEach(::println)
            .shouldBe(
                listOf(
                    LogicalLexerToken(
                        LexerToken(
                            type = WordToken.Word,
                            token = TextToken("Hello", 0, 4)
                        ),
                        startPosition = Line(1) to Offset(0),
                        endPosition = Line(1) to Offset(4)
                    ),
                    LogicalLexerToken(
                        LexerToken(
                            type = WordToken.NewLine,
                            token = TextToken("\n", 5, 5)
                        ),
                        startPosition = Line(2) to Offset(0),
                        endPosition = Line(2) to Offset(0)
                    ),
                    LogicalLexerToken(
                        LexerToken(
                            type = WordToken.Word,
                            token = TextToken("World", 6, 10)
                        ),
                        startPosition = Line(2) to Offset(0),
                        endPosition = Line(2) to Offset(4)
                    ),
                    LogicalLexerToken(
                        LexerToken(
                            type = WordToken.NewLine,
                            token = TextToken("\n", 11, 11)
                        ),
                        startPosition = Line(3) to Offset(0),
                        endPosition = Line(3) to Offset(0)
                    ),
                    LogicalLexerToken(
                        LexerToken(
                            type = WordToken.Word,
                            token = TextToken("FS", 12, 13)
                        ),
                        startPosition = Line(3) to Offset(0),
                        endPosition = Line(3) to Offset(1)
                    )
                )
            )
    }
})