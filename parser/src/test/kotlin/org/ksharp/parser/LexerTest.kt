package org.ksharp.parser

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.Line
import org.ksharp.common.Offset

enum class WordToken : TokenType {
    Word
}

fun Lexer<String>.consumeWord(): LexerToken {
    while (true) {
        val c = nextChar()
        if (c == null || !c.isLetter()) {
            return token(WordToken.Word, 1)
        }
    }
}

class LexerTest : StringSpec({
    "Given a lexer without rules should return unknown tokens" {
        "He Man".reader().lexer("") {
            null
        }.asSequence().toList().shouldBe(
            listOf(
                LexerToken(
                    type = BaseTokenType.Unknown,
                    token = TextToken("H", 0, 1),
                ),
                LexerToken(
                    type = BaseTokenType.Unknown,
                    token = TextToken("e", 1, 2),
                ),
                LexerToken(
                    type = BaseTokenType.Unknown,
                    token = TextToken(" ", 2, 3)
                ),
                LexerToken(
                    type = BaseTokenType.Unknown,
                    token = TextToken("M", 3, 4)
                ),
                LexerToken(
                    type = BaseTokenType.Unknown,
                    token = TextToken("a", 4, 5)
                ),
                LexerToken(
                    type = BaseTokenType.Unknown,
                    token = TextToken("n", 5, 6)
                )
            )
        )
    }
    "Given a lexer with a rule, should return tokens" {
        "He man".lexer("") {
            if (it.isLetter()) {
                consumeWord()
            } else null
        }.asSequence().toList().shouldBe(
            listOf(
                LexerToken(
                    type = WordToken.Word,
                    token = TextToken("He", 0, 2)
                ),
                LexerToken(
                    type = BaseTokenType.Unknown,
                    token = TextToken(" ", 2, 3)
                ),
                LexerToken(
                    type = WordToken.Word,
                    token = TextToken("man", 3, 6)
                ),
            )
        )
    }

    "Given a lexer, calling collapse should return just three tokens" {
        "He --- man".lexer("") {
            if (it.isLetter()) {
                consumeWord()
            } else null
        }.enableLookAhead()
            .collapseTokens()
            .asSequence().toList().shouldBe(
                listOf(
                    LexerToken(
                        type = WordToken.Word,
                        token = TextToken("He", 0, 2)
                    ),
                    LexerToken(
                        type = BaseTokenType.Unknown,
                        token = TextToken(" --- ", 2, 7)
                    ),
                    LexerToken(
                        type = WordToken.Word,
                        token = TextToken("man", 7, 10)
                    ),
                )
            )
    }

    "Given a lexer with newLines, convert tokens to logical tokens" {
        "Hello\nWorld\nFS".lexer("") {
            if (it.isLetter()) {
                consumeWord()
            } else if (it == '\n') {
                token(BaseTokenType.NewLine, 0)
            } else null
        }.toLogicalLexerToken()
            .asSequence()
            .toList().onEach(::println)
            .shouldBe(
                listOf(
                    LogicalLexerToken(
                        LexerToken(
                            type = WordToken.Word,
                            token = TextToken("Hello", 0, 5)
                        ),
                        startPosition = Line(1) to Offset(0),
                        endPosition = Line(1) to Offset(5)
                    ),
                    LogicalLexerToken(
                        LexerToken(
                            type = BaseTokenType.NewLine,
                            token = TextToken("\n", 5, 6)
                        ),
                        startPosition = Line(2) to Offset(0),
                        endPosition = Line(2) to Offset(1)
                    ),
                    LogicalLexerToken(
                        LexerToken(
                            type = WordToken.Word,
                            token = TextToken("World", 6, 11)
                        ),
                        startPosition = Line(2) to Offset(0),
                        endPosition = Line(2) to Offset(5)
                    ),
                    LogicalLexerToken(
                        LexerToken(
                            type = BaseTokenType.NewLine,
                            token = TextToken("\n", 11, 12)
                        ),
                        startPosition = Line(3) to Offset(0),
                        endPosition = Line(3) to Offset(1)
                    ),
                    LogicalLexerToken(
                        LexerToken(
                            type = WordToken.Word,
                            token = TextToken("FS", 12, 14)
                        ),
                        startPosition = Line(3) to Offset(0),
                        endPosition = Line(3) to Offset(2)
                    )
                )
            )
    }
})
