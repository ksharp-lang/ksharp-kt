package ksharp.parser

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll

class KSharpLexerTest : StringSpec({
    "Given lexer, check LowerCaseWord, UpperCaseWord, WhiteSpace token" {
        "type Name".lexer(kSharpTokenFactory)
            .toList()
            .shouldContainAll(
                LToken(
                    KSharpTokenType.LowerCaseWord,
                    TextToken("type", 0, 3)
                ),
                LToken(
                    KSharpTokenType.WhiteSpace,
                    TextToken(" ", 4, 4)
                ),
                LToken(
                    KSharpTokenType.UpperCaseWord,
                    TextToken("Name", 5, 8)
                )
            )
    }
    "Given lexer, check [], (), @" {
        "[](){}@,".lexer(kSharpTokenFactory)
            .toList()
            .shouldContainAll(
                LToken(KSharpTokenType.OpenBracket, TextToken("[", 0, 0)),
                LToken(KSharpTokenType.CloseBracket, TextToken("]", 1, 1)),
                LToken(KSharpTokenType.OpenParenthesis, TextToken("(", 2, 2)),
                LToken(KSharpTokenType.CloseParenthesis, TextToken(")", 3, 3)),
                LToken(KSharpTokenType.OpenCurlyBraces, TextToken("{", 4, 4)),
                LToken(KSharpTokenType.CloseCurlyBraces, TextToken("}", 5, 5)),
                LToken(KSharpTokenType.Alt, TextToken("@", 6, 6)),
                LToken(KSharpTokenType.Comma, TextToken(",", 7, 7))
            )
    }
    "Given lexer, check operators" {
        "+-*/%><=!&$#^?.\\|".lexer(kSharpTokenFactory)
            .toList()
            .shouldContainAll(
                LToken(KSharpTokenType.Operator, TextToken("+-*/%><=!&$#^?.\\|", 0, 16)),
            )
    }
    "Given lexer, check integers, decimals, integer and dot operator" {
        "100 1.3 .6 2.".lexer(kSharpTokenFactory)
            .toList().also(::println)
            .shouldContainAll(
                LToken(KSharpTokenType.Integer, TextToken("100", 0, 2)),
                LToken(KSharpTokenType.Float, TextToken("1.3", 4, 6)),
                LToken(KSharpTokenType.Float, TextToken(".6", 8, 9)),
                LToken(KSharpTokenType.Integer, TextToken("2", 11, 11)),
                LToken(KSharpTokenType.Operator, TextToken(".", 12, 12)),
            )
    }
})
