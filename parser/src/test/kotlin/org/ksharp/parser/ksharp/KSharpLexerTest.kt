package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import org.ksharp.parser.LexerToken
import org.ksharp.parser.TextToken

class KSharpLexerTest : StringSpec({
    "Given lexer, check LowerCaseWord, UpperCaseWord, WhiteSpace token" {
        "type Name".kSharpLexer()
            .asSequence()
            .toList()
            .shouldContainAll(
                LexerToken(
                    KSharpTokenType.LowerCaseWord,
                    TextToken("type", 0, 3)
                ),
                LexerToken(
                    KSharpTokenType.WhiteSpace,
                    TextToken(" ", 4, 4)
                ),
                LexerToken(
                    KSharpTokenType.UpperCaseWord,
                    TextToken("Name", 5, 8)
                )
            )
    }
    "Given lexer, check [], (), @" {
        "[](){}@,".kSharpLexer()
            .asSequence()
            .toList()
            .shouldContainAll(
                LexerToken(KSharpTokenType.OpenBracket, TextToken("[", 0, 0)),
                LexerToken(KSharpTokenType.CloseBracket, TextToken("]", 1, 1)),
                LexerToken(KSharpTokenType.OpenParenthesis, TextToken("(", 2, 2)),
                LexerToken(KSharpTokenType.CloseParenthesis, TextToken(")", 3, 3)),
                LexerToken(KSharpTokenType.OpenCurlyBraces, TextToken("{", 4, 4)),
                LexerToken(KSharpTokenType.CloseCurlyBraces, TextToken("}", 5, 5)),
                LexerToken(KSharpTokenType.Alt, TextToken("@", 6, 6)),
                LexerToken(KSharpTokenType.Comma, TextToken(",", 7, 7))
            )
    }
    "Given lexer, check operators" {
        "+-*/%><=!&$#^?.\\|".kSharpLexer()
            .asSequence()
            .toList()
            .shouldContainAll(
                LexerToken(KSharpTokenType.Operator, TextToken("+-*/%><=!&$#^?.\\|", 0, 16)),
            )
    }
    "Given lexer, check integers, decimals, integer and dot operator" {
        "100 1.3 .6 2.".kSharpLexer()
            .asSequence()
            .toList().also(::println)
            .shouldContainAll(
                LexerToken(KSharpTokenType.Integer, TextToken("100", 0, 2)),
                LexerToken(KSharpTokenType.Float, TextToken("1.3", 4, 6)),
                LexerToken(KSharpTokenType.Float, TextToken(".6", 8, 9)),
                LexerToken(KSharpTokenType.Integer, TextToken("2", 11, 11)),
                LexerToken(KSharpTokenType.Operator, TextToken(".", 12, 12)),
            )
    }
    "Given a lexer, check collapse tokens, should remove whitespace only" {
        "import ksharp.test as math".kSharpLexer()
            .collapseKSharpTokens()
            .asSequence()
            .toList()
            .shouldContainAll(
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("import", 0, 5)),
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("ksharp", 7, 12)),
                LexerToken(KSharpTokenType.Operator, TextToken(".", 13, 13)),
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("test", 14, 17)),
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("as", 19, 20)),
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("math", 22, 25))
            )
    }
    "Given a lexer, check collapse tokens to form function tokens" {
        "internal->wire.name  ->  wire".kSharpLexer()
            .collapseKSharpTokens()
            .asSequence()
            .toList()
            .shouldContainAll(
                LexerToken(KSharpTokenType.FunctionName, TextToken("internal->wire", 0, 13)),
                LexerToken(KSharpTokenType.Operator, TextToken(".", 14, 14)),
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("name", 15, 18)),
                LexerToken(KSharpTokenType.Operator3, TextToken("->", 21, 22)),
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("wire", 25, 28)),
            )
    }
    "Given a lexer, check collapse tokens, should leave really important whitespaces (those after a newline) inside the NewLine token" {
        "internal->wire.name = \n    10".kSharpLexer()
            .collapseKSharpTokens()
            .asSequence()
            .toList().onEach { println("+++ $it") }
            .shouldContainAll(
                LexerToken(KSharpTokenType.FunctionName, TextToken("internal->wire", 0, 13)),
                LexerToken(KSharpTokenType.Operator, TextToken(".", 14, 14)),
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("name", 15, 18)),
                LexerToken(KSharpTokenType.Operator12, TextToken("=", 20, 20)),
                LexerToken(KSharpTokenType.NewLine, TextToken("\n", 22, 22)),
                LexerToken(KSharpTokenType.WhiteSpace, TextToken("    ", 23, 26)),
                LexerToken(KSharpTokenType.Integer, TextToken("10", 27, 28)),
            )
    }
    "Given a lexer, map operators" {
        "** *>> //> %%% +++ - << >> <== != & ||| ^& && || = . # $ ?".kSharpLexer()
            .collapseKSharpTokens()
            .asSequence()
            .toList()
            .shouldContainAll(
                LexerToken(
                    type = KSharpTokenType.Operator1,
                    token = TextToken(text = "**", startOffset = 0, endOffset = 1)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator2,
                    token = TextToken(text = "*>>", startOffset = 3, endOffset = 5)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator2,
                    token = TextToken(text = "//>", startOffset = 7, endOffset = 9)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator2,
                    token = TextToken(text = "%%%", startOffset = 11, endOffset = 13)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator3,
                    token = TextToken(text = "+++", startOffset = 15, endOffset = 17)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator3,
                    token = TextToken(text = "-", startOffset = 19, endOffset = 19)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator4,
                    token = TextToken(text = "<<", startOffset = 21, endOffset = 22)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator4,
                    token = TextToken(text = ">>", startOffset = 24, endOffset = 25)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator5,
                    token = TextToken(text = "<==", startOffset = 27, endOffset = 29)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator6,
                    token = TextToken(text = "!=", startOffset = 31, endOffset = 32)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator7,
                    token = TextToken(text = "&", startOffset = 34, endOffset = 34)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator9,
                    token = TextToken(text = "|||", startOffset = 36, endOffset = 38)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator8,
                    token = TextToken(text = "^&", startOffset = 40, endOffset = 41)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator10,
                    token = TextToken(text = "&&", startOffset = 43, endOffset = 44)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator11,
                    token = TextToken(text = "||", startOffset = 46, endOffset = 47)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator12,
                    token = TextToken(text = "=", startOffset = 49, endOffset = 49)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator,
                    token = TextToken(text = ".", startOffset = 51, endOffset = 51)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator,
                    token = TextToken(text = "#", startOffset = 53, endOffset = 53)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator,
                    token = TextToken(text = "$", startOffset = 55, endOffset = 55)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator,
                    token = TextToken(text = "?", startOffset = 57, endOffset = 57)
                )
            )
    }
})

class KSharpLexerMarkExpressionsTest : ShouldSpec({
    val endExpression: (Int) -> LexerToken = { index ->
        LexerToken(
            type = KSharpTokenType.EndExpression,
            token = TextToken("$index", 0, 0)
        )
    }
    context("With just one expression without new line") {
        "type Int = Integer"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markExpressions(endExpression)
            .apply {
                should("Should return one expression") {
                    asSequence()
                        .toList()
                        .shouldBe(
                            listOf(
                                LexerToken(
                                    type = KSharpTokenType.LowerCaseWord,
                                    token = TextToken(text = "type", startOffset = 0, endOffset = 3)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.UpperCaseWord,
                                    token = TextToken(text = "Int", startOffset = 5, endOffset = 7)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.Operator12,
                                    token = TextToken(text = "=", startOffset = 9, endOffset = 9)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.UpperCaseWord,
                                    token = TextToken(text = "Integer", startOffset = 11, endOffset = 17)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.EndExpression,
                                    token = TextToken(text = "1", startOffset = 0, endOffset = 0)
                                )
                            )
                        )
                }
            }
    }
    context("With just one expression and new line at end") {
        "type Int = Integer\n"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markExpressions(endExpression)
            .apply {
                should("Should return one expression") {
                    asSequence()
                        .filter { it.type == KSharpTokenType.EndExpression }
                        .count().shouldBe(1)
                }
            }
    }
    context("With just one expression but with new lines and spaces") {
        """type
           | Int =
           | Integer
        """.trimMargin().kSharpLexer()
            .collapseKSharpTokens()
            .markExpressions(endExpression)
            .apply {
                should("Should return one expression") {
                    asSequence()
                        .filter { it.type == KSharpTokenType.EndExpression }
                        .count().shouldBe(1)
                }
            }
    }
    context("With many expression") {
        """type Int = Integer
          |
          |
          |type 
          | Int =
          | Integer
        """.trimMargin().kSharpLexer()
            .collapseKSharpTokens()
            .markExpressions(endExpression)
            .apply {
                should("Should return two expression") {
                    asSequence()
                        .filter { it.type == KSharpTokenType.EndExpression }
                        .count().shouldBe(2)
                }
            }
    }
    context("With nested expressions") {
        """type Int = Integer
          |
          |
          |let sum3 a = 
          | let x = 3
          |   a + 3
        """.trimMargin().kSharpLexer()
            .collapseKSharpTokens()
            .markExpressions(endExpression)
            .apply {
                should("Should return three expression") {
                    asSequence()
                        .toList()
                        .onEach(::println)
                        .shouldBe(
                            listOf(
                                LexerToken(
                                    type = KSharpTokenType.LowerCaseWord,
                                    token = TextToken(text = "type", startOffset = 0, endOffset = 3)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.UpperCaseWord,
                                    token = TextToken(text = "Int", startOffset = 5, endOffset = 7)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.Operator12,
                                    token = TextToken(text = "=", startOffset = 9, endOffset = 9)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.UpperCaseWord,
                                    token = TextToken(text = "Integer", startOffset = 11, endOffset = 17)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.EndExpression,
                                    token = TextToken(text = "1", startOffset = 0, endOffset = 0)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.LowerCaseWord,
                                    token = TextToken(text = "let", startOffset = 21, endOffset = 23)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.LowerCaseWord,
                                    token = TextToken(text = "sum3", startOffset = 25, endOffset = 28)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.LowerCaseWord,
                                    token = TextToken(text = "a", startOffset = 30, endOffset = 30)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.Operator12,
                                    token = TextToken(text = "=", startOffset = 32, endOffset = 32)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.LowerCaseWord,
                                    token = TextToken(text = "let", startOffset = 36, endOffset = 38)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.LowerCaseWord,
                                    token = TextToken(text = "x", startOffset = 40, endOffset = 40)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.Operator12,
                                    token = TextToken(text = "=", startOffset = 42, endOffset = 42)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.Integer,
                                    token = TextToken(text = "3", startOffset = 44, endOffset = 44)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.LowerCaseWord,
                                    token = TextToken(text = "a", startOffset = 49, endOffset = 49)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.Operator3,
                                    token = TextToken(text = "+", startOffset = 51, endOffset = 51)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.Integer,
                                    token = TextToken(text = "3", startOffset = 53, endOffset = 53)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.EndExpression,
                                    token = TextToken(text = "2", startOffset = 0, endOffset = 0)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.EndExpression,
                                    token = TextToken(text = "3", startOffset = 0, endOffset = 0)
                                )
                            )
                        )
                }
            }
    }
    context("With nested expressions 2") {
        """type Int = Integer
          |
          |
          |let sum3 a = 
          | let x = 3
          |   a + 3
          | println 10
        """.trimMargin().kSharpLexer()
            .collapseKSharpTokens()
            .markExpressions(endExpression)
            .apply {
                should("Should return three expression") {
                    asSequence()
                        .toList()
                        .onEach(::println)
                        .shouldBe(
                            listOf(
                                LexerToken(
                                    type = KSharpTokenType.LowerCaseWord,
                                    token = TextToken(text = "type", startOffset = 0, endOffset = 3)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.UpperCaseWord,
                                    token = TextToken(text = "Int", startOffset = 5, endOffset = 7)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.Operator12,
                                    token = TextToken(text = "=", startOffset = 9, endOffset = 9)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.UpperCaseWord,
                                    token = TextToken(text = "Integer", startOffset = 11, endOffset = 17)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.EndExpression,
                                    token = TextToken(text = "1", startOffset = 0, endOffset = 0)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.LowerCaseWord,
                                    token = TextToken(text = "let", startOffset = 21, endOffset = 23)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.LowerCaseWord,
                                    token = TextToken(text = "sum3", startOffset = 25, endOffset = 28)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.LowerCaseWord,
                                    token = TextToken(text = "a", startOffset = 30, endOffset = 30)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.Operator12,
                                    token = TextToken(text = "=", startOffset = 32, endOffset = 32)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.LowerCaseWord,
                                    token = TextToken(text = "let", startOffset = 36, endOffset = 38)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.LowerCaseWord,
                                    token = TextToken(text = "x", startOffset = 40, endOffset = 40)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.Operator12,
                                    token = TextToken(text = "=", startOffset = 42, endOffset = 42)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.Integer,
                                    token = TextToken(text = "3", startOffset = 44, endOffset = 44)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.LowerCaseWord,
                                    token = TextToken(text = "a", startOffset = 49, endOffset = 49)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.Operator3,
                                    token = TextToken(text = "+", startOffset = 51, endOffset = 51)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.Integer,
                                    token = TextToken(text = "3", startOffset = 53, endOffset = 53)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.EndExpression,
                                    token = TextToken(text = "2", startOffset = 0, endOffset = 0)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.LowerCaseWord,
                                    token = TextToken(text = "println", startOffset = 56, endOffset = 62)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.Integer,
                                    token = TextToken(text = "10", startOffset = 64, endOffset = 65)
                                ),
                                LexerToken(
                                    type = KSharpTokenType.EndExpression,
                                    token = TextToken(text = "3", startOffset = 0, endOffset = 0)
                                )
                            )
                        )
                }
            }
    }
})
