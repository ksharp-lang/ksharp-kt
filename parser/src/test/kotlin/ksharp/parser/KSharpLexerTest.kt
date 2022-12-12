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
})
