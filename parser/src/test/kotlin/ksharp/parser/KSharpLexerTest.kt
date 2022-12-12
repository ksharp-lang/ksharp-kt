package ksharp.parser

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll

class KSharpLexerTest : StringSpec({
    "Given lexer, check LowerCaseWord, UpperCaseWord, WhiteSpace token" {
        "type Name".lexer(kSharpTokenFactory)
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
        "[](){}@,".lexer(kSharpTokenFactory)
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
        "+-*/%><=!&$#^?.\\|".lexer(kSharpTokenFactory)
            .asSequence()
            .toList()
            .shouldContainAll(
                LexerToken(KSharpTokenType.Operator, TextToken("+-*/%><=!&$#^?.\\|", 0, 16)),
            )
    }
    "Given lexer, check integers, decimals, integer and dot operator" {
        "100 1.3 .6 2.".lexer(kSharpTokenFactory)
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

    "Given a lexer, check collapse tokens to form function tokens" {
        "internal->wire.name  ->  wire".lexer(kSharpTokenFactory)
            .collapseKSharpTokens()
            .asSequence()
            .toList().also(::println)
            .shouldContainAll(
                LexerToken(KSharpTokenType.FunctionName, TextToken("internal->wire", 0, 13)),
                LexerToken(KSharpTokenType.Operator, TextToken(".", 14, 14)),
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("name", 15, 18)),
                LexerToken(KSharpTokenType.Operator3, TextToken("->", 21, 22)),
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("wire", 25, 28)),
            )
    }

    "Given a lexer, check collapse tokens, should leave really important whitespaces (those after a newline)" {
        "internal->wire.name = \n    10".lexer(kSharpTokenFactory)
            .collapseKSharpTokens()
            .asSequence()
            .toList().onEach(::println)
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
        "** *>> //> %%% +++ - << >> <== != & ||| ^& && || = . # $ ?".lexer(kSharpTokenFactory)
            .collapseKSharpTokens()
            .asSequence()
            .toList().onEach(::println)
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
