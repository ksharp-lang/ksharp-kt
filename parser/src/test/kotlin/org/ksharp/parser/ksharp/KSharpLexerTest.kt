package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.sequences.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.ksharp.parser.*

class KSharpLexerTest : StringSpec({
    "Check escaope characters" {
        "t'\"rnf\\b".asSequence().map { it.isEscapeCharacter() }.filter { !it }.shouldBeEmpty()
    }
    "Given lexer, check LowerCaseWord, UpperCaseWord, WhiteSpace, Label, Operator token" {
        "type Name lbl: Name: User_name".kSharpLexer()
            .enableLabelToken {
                it.asSequence()
                    .toList()
                    .shouldBe(
                        listOf(
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
                            ),
                            LexerToken(
                                KSharpTokenType.WhiteSpace,
                                TextToken(" ", 9, 9)
                            ),
                            LexerToken(
                                KSharpTokenType.Label,
                                TextToken("lbl:", 10, 13)
                            ),
                            LexerToken(
                                KSharpTokenType.WhiteSpace,
                                TextToken(" ", 14, 14)
                            ),
                            LexerToken(
                                KSharpTokenType.UpperCaseWord,
                                TextToken("Name", 15, 18)
                            ),
                            LexerToken(
                                KSharpTokenType.Operator,
                                TextToken(":", 19, 19)
                            ),
                            LexerToken(
                                type = KSharpTokenType.WhiteSpace,
                                token = TextToken(text = " ", startOffset = 20, endOffset = 20)
                            ),
                            LexerToken(
                                KSharpTokenType.UpperCaseWord,
                                TextToken("User_name", 21, 29)
                            ),
                        )
                    )
            }
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
    "Given lexer, check operator function name, ( 1" {
        "(+) (1"
            .kSharpLexer()
            .asSequence()
            .toList()
            .onEach(::println)
            .shouldContainAll(
                LexerToken(
                    type = KSharpTokenType.OperatorFunctionName,
                    token = TextToken(text = "(+)", startOffset = 0, endOffset = 2)
                ),
                LexerToken(
                    type = KSharpTokenType.OpenParenthesis,
                    token = TextToken(text = "(", startOffset = 4, endOffset = 4)
                ),
                LexerToken(
                    type = KSharpTokenType.Integer,
                    token = TextToken(text = "1", startOffset = 5, endOffset = 5)
                )
            )
    }
    "Given lexer, check #( #+ #{, #[" {
        "#( #+ #{, #[".kSharpLexer()
            .asSequence()
            .toList()
            .onEach(::println)
            .shouldContainAll(
                LexerToken(KSharpTokenType.Operator, TextToken("#", 0, 0)),
                LexerToken(KSharpTokenType.OpenParenthesis, TextToken("(", 1, 1)),
                LexerToken(KSharpTokenType.Operator, TextToken("#+", 3, 4)),
                LexerToken(KSharpTokenType.Operator, TextToken("#", 6, 6)),
                LexerToken(KSharpTokenType.OpenCurlyBraces, TextToken("{", 7, 7)),
                LexerToken(KSharpTokenType.OpenSetBracket, TextToken("#[", 10, 11)),
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
    "Given lexer, check decimal" {
        "1.6"
            .kSharpLexer()
            .asSequence()
            .toList().onEach(::println).shouldContainAll(
                LexerToken(KSharpTokenType.Float, TextToken("1.6", 0, 2))
            )
    }
    "Given lexer, check integers, decimals, integer and dot operator" {
        "100 1.3 .6 2. 1_000 0xFFFFbB 0b110011 0o12345 010 ".kSharpLexer()
            .asSequence()
            .toList().onEach(::println)
            .shouldContainAll(
                LexerToken(KSharpTokenType.Integer, TextToken("100", 0, 2)),
                LexerToken(KSharpTokenType.Float, TextToken("1.3", 4, 6)),
                LexerToken(KSharpTokenType.Float, TextToken(".6", 8, 9)),
                LexerToken(KSharpTokenType.Integer, TextToken("2", 11, 11)),
                LexerToken(KSharpTokenType.Operator, TextToken(".", 12, 12)),
                LexerToken(KSharpTokenType.Integer, TextToken("1_000", 14, 18)),
                LexerToken(KSharpTokenType.HexInteger, TextToken("0xFFFFbB", 20, 27)),
                LexerToken(
                    KSharpTokenType.BinaryInteger,
                    TextToken(text = "0b110011", startOffset = 29, endOffset = 36)
                ),
                LexerToken(
                    KSharpTokenType.OctalInteger,
                    TextToken(text = "0o12345", startOffset = 38, endOffset = 44)
                ),
                LexerToken(
                    KSharpTokenType.Integer,
                    TextToken(text = "010", startOffset = 46, endOffset = 48)
                )
            )
    }
    "Given lexer, check hex, binary, octal" {
        "0xFF0F".kSharpLexer()
            .asSequence()
            .toList().onEach(::println)
            .shouldContainAll(
                LexerToken(KSharpTokenType.HexInteger, TextToken("0xFF0F", 0, 5)),
            )

        "0xFF_FF".kSharpLexer()
            .asSequence()
            .toList().onEach(::println)
            .shouldContainAll(
                LexerToken(KSharpTokenType.HexInteger, TextToken("0xFF_FF", 0, 6)),
            )
        "0b0001_1001".kSharpLexer()
            .asSequence()
            .toList().onEach(::println)
            .shouldContainAll(
                LexerToken(KSharpTokenType.BinaryInteger, TextToken("0b0001_1001", 0, 10)),
            )
        "0b0011".kSharpLexer()
            .asSequence()
            .toList().onEach(::println)
            .shouldContainAll(
                LexerToken(KSharpTokenType.BinaryInteger, TextToken("0b0011", 0, 5)),
            )
        "0o0011_6471".kSharpLexer()
            .asSequence()
            .toList().onEach(::println)
            .shouldContainAll(
                LexerToken(KSharpTokenType.OctalInteger, TextToken("0o0011_6471", 0, 10)),
            )
        "0o1573".kSharpLexer()
            .asSequence()
            .toList().onEach(::println)
            .shouldContainAll(
                LexerToken(KSharpTokenType.OctalInteger, TextToken("0o1573", 0, 5)),
            )
    }
    "Given a lexer, check collapse tokens, should remove whitespace only" {
        "import ksharp .test as math".kSharpLexer()
            .collapseKSharpTokens()
            .asSequence()
            .toList()
            .shouldContainAll(
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("import", 0, 5)),
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("ksharp", 7, 12)),
                LexerToken(KSharpTokenType.Operator0, TextToken(".", 14, 14)),
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("test", 15, 18)),
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("as", 20, 21)),
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("math", 23, 26))
            )
    }
    "Given a lexer, check collapse tokens to form function tokens" {
        "internal->wire.name  ->  wire data.list map . test".kSharpLexer()
            .collapseKSharpTokens()
            .asSequence()
            .toList().onEach(::println)
            .shouldContainAll(
                LexerToken(KSharpTokenType.FunctionName, TextToken("internal->wire", 0, 13)),
                LexerToken(KSharpTokenType.Operator0, TextToken(".", 14, 14)),
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("name", 15, 18)),
                LexerToken(KSharpTokenType.Operator10, TextToken("->", 21, 22)),
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("wire", 25, 28)),
                LexerToken(KSharpTokenType.FunctionName, TextToken("data.list", 30, 38)),
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("map", 40, 42)),
                LexerToken(KSharpTokenType.Operator0, TextToken(".", 44, 44)),
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("test", 46, 49)),
            )
    }
    "Given a lexer, check collapse tokens, should leave really important whitespaces (those after a newline) inside the NewLine token" {
        "internal->wire.name = \n    10".kSharpLexer()
            .collapseKSharpTokens()
            .asSequence()
            .toList().onEach { println("+++ $it") }
            .shouldContainAll(
                LexerToken(KSharpTokenType.FunctionName, TextToken("internal->wire", 0, 13)),
                LexerToken(KSharpTokenType.Operator0, TextToken(".", 14, 14)),
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("name", 15, 18)),
                LexerToken(KSharpTokenType.AssignOperator, TextToken("=", 20, 20)),
                LexerToken(KSharpTokenType.NewLine, TextToken("\n    ", 22, 26)),
                LexerToken(KSharpTokenType.Integer, TextToken("10", 27, 28)),
                LexerToken(KSharpTokenType.NewLine, TextToken("\n", 29, 29)),
            )
    }
    "Given a lexer, map operators" {
        "** *>> //> %%% +++ - << >> <== != & ||| ^& && || = . # $ ? :".kSharpLexer()
            .collapseKSharpTokens()
            .asSequence()
            .toList()
            .shouldContainAll(
                LexerToken(
                    type = KSharpTokenType.Operator12,
                    token = TextToken(text = "**", startOffset = 0, endOffset = 1)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator11,
                    token = TextToken(text = "*>>", startOffset = 3, endOffset = 5)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator11,
                    token = TextToken(text = "//>", startOffset = 7, endOffset = 9)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator11,
                    token = TextToken(text = "%%%", startOffset = 11, endOffset = 13)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator10,
                    token = TextToken(text = "+++", startOffset = 15, endOffset = 17)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator10,
                    token = TextToken(text = "-", startOffset = 19, endOffset = 19)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator9,
                    token = TextToken(text = "<<", startOffset = 21, endOffset = 22)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator9,
                    token = TextToken(text = ">>", startOffset = 24, endOffset = 25)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator8,
                    token = TextToken(text = "<==", startOffset = 27, endOffset = 29)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator7,
                    token = TextToken(text = "!=", startOffset = 31, endOffset = 32)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator6,
                    token = TextToken(text = "&", startOffset = 34, endOffset = 34)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator4,
                    token = TextToken(text = "|||", startOffset = 36, endOffset = 38)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator5,
                    token = TextToken(text = "^&", startOffset = 40, endOffset = 41)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator3,
                    token = TextToken(text = "&&", startOffset = 43, endOffset = 44)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator2,
                    token = TextToken(text = "||", startOffset = 46, endOffset = 47)
                ),
                LexerToken(
                    type = KSharpTokenType.AssignOperator,
                    token = TextToken(text = "=", startOffset = 49, endOffset = 49)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator0,
                    token = TextToken(text = ".", startOffset = 51, endOffset = 51)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator,
                    token = TextToken(text = "#", startOffset = 53, endOffset = 53)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator1,
                    token = TextToken(text = "$", startOffset = 55, endOffset = 55)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator,
                    token = TextToken(text = "?", startOffset = 57, endOffset = 57)
                ),
                LexerToken(
                    type = KSharpTokenType.Operator,
                    token = TextToken(text = ":", startOffset = 59, endOffset = 59)
                )
            )
    }
    "Given a lexer, map character and string" {
        "'a' \"Hello World\" \"\"\"Hello\nWorld\"\"\" \"\" '\\\''  \"\\\"\""
            .kSharpLexer()
            .collapseKSharpTokens()
            .asSequence()
            .toList()
            .onEach(::println)
            .shouldBe(
                listOf(
                    LexerToken(
                        type = KSharpTokenType.Character,
                        token = TextToken(text = "'a'", startOffset = 0, endOffset = 2)
                    ),
                    LexerToken(
                        type = KSharpTokenType.String,
                        token = TextToken(text = "\"Hello World\"", startOffset = 4, endOffset = 16)
                    ),
                    LexerToken(
                        type = KSharpTokenType.MultiLineString,
                        token = TextToken(text = "\"\"\"Hello\nWorld\"\"\"", startOffset = 18, endOffset = 34)
                    ),
                    LexerToken(
                        type = KSharpTokenType.String,
                        token = TextToken(text = "\"\"", startOffset = 36, endOffset = 37)
                    ),
                    LexerToken(
                        type = KSharpTokenType.Character,
                        token = TextToken(text = "'\\''", startOffset = 39, endOffset = 42)
                    ),
                    LexerToken(
                        type = KSharpTokenType.String,
                        token = TextToken(text = "\"\\\"\"", startOffset = 45, endOffset = 48)
                    ),
                    LexerToken(
                        type = KSharpTokenType.NewLine,
                        token = TextToken(text = "\n", startOffset = 49, endOffset = 49)
                    ),
                )
            )
    }
    "Given a lexer, map invalid characters" {
        "'".kSharpLexer().collapseKSharpTokens().asSequence().toList()
            .shouldBe(
                listOf(
                    LexerToken(
                        type = KSharpTokenType.Character,
                        token = TextToken(text = "'", startOffset = 0, endOffset = 0)
                    ),
                    LexerToken(
                        type = KSharpTokenType.NewLine,
                        token = TextToken(text = "\n", startOffset = 1, endOffset = 1)
                    ),
                )
            )

        "'a".kSharpLexer().collapseKSharpTokens().asSequence().toList()
            .shouldBe(
                listOf(
                    LexerToken(
                        type = KSharpTokenType.Character,
                        token = TextToken(text = "'a", startOffset = 0, endOffset = 1)
                    ),
                    LexerToken(
                        type = KSharpTokenType.NewLine,
                        token = TextToken(text = "\n", startOffset = 2, endOffset = 2)
                    ),
                )
            )
    }
    "Given a lexer, map invalid strings" {
        "\"".kSharpLexer().collapseKSharpTokens().asSequence().toList()
            .shouldBe(
                listOf(
                    LexerToken(
                        type = KSharpTokenType.String,
                        token = TextToken(text = "\"", startOffset = 0, endOffset = 0)
                    ),
                    LexerToken(
                        type = KSharpTokenType.NewLine,
                        token = TextToken(text = "\n", startOffset = 1, endOffset = 1)
                    ),
                )
            )

        "\"a".kSharpLexer().collapseKSharpTokens().asSequence().toList()
            .shouldBe(
                listOf(
                    LexerToken(
                        type = KSharpTokenType.String,
                        token = TextToken(text = "\"a", startOffset = 0, endOffset = 1)
                    ),
                    LexerToken(
                        type = KSharpTokenType.NewLine,
                        token = TextToken(text = "\n", startOffset = 2, endOffset = 2)
                    ),
                )
            )

        "\"\"\"".kSharpLexer().collapseKSharpTokens().asSequence().toList()
            .shouldBe(
                listOf(
                    LexerToken(
                        type = KSharpTokenType.MultiLineString,
                        token = TextToken(text = "\"\"\"", startOffset = 0, endOffset = 2)
                    ),
                    LexerToken(
                        type = KSharpTokenType.NewLine,
                        token = TextToken(text = "\n", startOffset = 3, endOffset = 3)
                    ),
                )
            )
    }
    "Given a lexer, map tabs as whitespaces" {
        "\t".kSharpLexer()
            .asSequence()
            .toList()
            .shouldBe(listOf(LexerToken(KSharpTokenType.WhiteSpace, TextToken("\t", 0, 0))))
    }
    "Given a lexer, ignore \r" {
        "\r".kSharpLexer()
            .asSequence()
            .toList()
            .shouldBeEmpty()
    }
    "if then else mapIfThenKeyword disabled" {
        "if then else"
            .kSharpLexer()
            .collapseKSharpTokens()
            .asSequence().toList()
            .shouldContainAll(
                LexerToken(
                    type = KSharpTokenType.If,
                    token = TextToken(text = "if", startOffset = 0, endOffset = 1)
                ),
                LexerToken(
                    type = KSharpTokenType.LowerCaseWord,
                    token = TextToken(text = "then", startOffset = 3, endOffset = 6)
                ),
                LexerToken(
                    type = KSharpTokenType.LowerCaseWord,
                    token = TextToken(text = "else", startOffset = 8, endOffset = 11)
                )
            )
    }
    "if then else mapIfThenKeyword enabled" {
        "if then else"
            .kSharpLexer()
            .collapseKSharpTokens()
            .enableMapElseThenKeywords {
                it.asSequence().toList()
            }
            .shouldContainAll(
                LexerToken(
                    type = KSharpTokenType.If,
                    token = TextToken(text = "if", startOffset = 0, endOffset = 1)
                ),
                LexerToken(
                    type = KSharpTokenType.Then,
                    token = TextToken(text = "then", startOffset = 3, endOffset = 6)
                ),
                LexerToken(
                    type = KSharpTokenType.Else,
                    token = TextToken(text = "else", startOffset = 8, endOffset = 11)
                )
            )
    }
})

private fun Sequence<Token>.asStringSequence() = map {
    when (it.type) {
        KSharpTokenType.NewLine -> "NewLine"
        KSharpTokenType.BeginBlock -> "BeginBlock"
        KSharpTokenType.EndBlock -> "EndBlock"
        else -> "${it.type}:${it.text}"
    }
}

private fun List<String>.printTokens() = onEach { println("\"$it\",") }

class KSharpLexerMarkBlocksTest : ShouldSpec({
    val endExpression: (TokenType) -> LexerToken = {
        LexerToken(
            type = it,
            token = TextToken("", 0, 0)
        )
    }
    context("With just one block without new line") {
        "type Int = Integer"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks(endExpression)
            .apply {
                should("Should return one expression") {
                    asSequence()
                        .asStringSequence()
                        .toList()
                        .printTokens()
                        .shouldBe(
                            listOf(
                                "BeginBlock",
                                "LowerCaseWord:type",
                                "UpperCaseWord:Int",
                                "AssignOperator:=",
                                "UpperCaseWord:Integer",
                                "NewLine",
                                "EndBlock",
                            )
                        )
                }
            }
    }
    context("With just one block and new line at end") {
        "type Int = Integer\n"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks(endExpression)
            .apply {
                should("Should return one expression") {
                    asSequence()
                        .asStringSequence()
                        .toList()
                        .printTokens()
                        .shouldBe(
                            listOf(
                                "BeginBlock",
                                "LowerCaseWord:type",
                                "UpperCaseWord:Int",
                                "AssignOperator:=",
                                "UpperCaseWord:Integer",
                                "NewLine",
                                "EndBlock"
                            )
                        )
                }
            }
    }
    context("With just one block but with new lines and spaces") {
        """type
           | Int =
           | Integer
        """.trimMargin().kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks(endExpression)
            .apply {
                should("Should return one expression") {
                    asSequence()
                        .asStringSequence()
                        .toList()
                        .printTokens()
                        .shouldBe(
                            listOf(
                                "BeginBlock",
                                /**/"LowerCaseWord:type",
                                /**/"BeginBlock",
                                /**//**/"UpperCaseWord:Int",
                                /**//**/"AssignOperator:=",
                                /**//**/"NewLine",
                                /**//**/"UpperCaseWord:Integer",
                                /**//**/"NewLine",
                                /**/"EndBlock",
                                /**/"NewLine",
                                "EndBlock"
                            )
                        )
                }
            }
    }
    context("With just one block but with new lines and spaces with discard block and new line tokens") {
        """type
           | Int =
           | Integer
        """.trimMargin().kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks(endExpression)
            .enableDiscardBlockAndNewLineTokens {
                it.asSequence()
                    .asStringSequence()
                    .toList()
            }
            .apply {
                should("Should return one expression") {
                    printTokens()
                        .shouldBe(
                            listOf(
                                /**/"LowerCaseWord:type",
                                /**//**/"UpperCaseWord:Int",
                                /**//**/"AssignOperator:=",
                                /**//**/"UpperCaseWord:Integer",
                            )
                        )
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
            .markBlocks(endExpression)
            .apply {
                should("Should return three blocks") {
                    asSequence()
                        .asStringSequence()
                        .toList()
                        .printTokens()
                        .shouldBe(
                            listOf(
                                "BeginBlock",
                                "LowerCaseWord:type",
                                "UpperCaseWord:Int",
                                "AssignOperator:=",
                                "UpperCaseWord:Integer",
                                "NewLine",
                                "EndBlock",

                                "BeginBlock",
                                "LowerCaseWord:type",
                                "BeginBlock",
                                "UpperCaseWord:Int",
                                "AssignOperator:=",
                                "NewLine",
                                "UpperCaseWord:Integer",
                                "NewLine",
                                "EndBlock",
                                "NewLine",
                                "EndBlock",
                            )
                        )
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
            .markBlocks(endExpression)
            .apply {
                should("Should return three expression") {
                    asSequence()
                        .asStringSequence()
                        .toList()
                        .printTokens()
                        .shouldBe(
                            listOf(
                                "BeginBlock",
                                "LowerCaseWord:type",
                                "UpperCaseWord:Int",
                                "AssignOperator:=",
                                "UpperCaseWord:Integer",
                                "NewLine",
                                "EndBlock",
                                "BeginBlock",
                                "LowerCaseWord:let",
                                "LowerCaseWord:sum3",
                                "LowerCaseWord:a",
                                "AssignOperator:=",
                                "BeginBlock",
                                "LowerCaseWord:let",
                                "LowerCaseWord:x",
                                "AssignOperator:=",
                                "Integer:3",
                                "BeginBlock",
                                "LowerCaseWord:a",
                                "Operator10:+",
                                "Integer:3",
                                "NewLine",
                                "EndBlock",
                                "NewLine",
                                "EndBlock",
                                "NewLine",
                                "EndBlock",
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
            .markBlocks(endExpression)
            .apply {
                should("Should return three expression") {
                    asSequence()
                        .asStringSequence()
                        .toList()
                        .printTokens()
                        .shouldBe(
                            listOf(
                                "BeginBlock",
                                "LowerCaseWord:type",
                                "UpperCaseWord:Int",
                                "AssignOperator:=",
                                "UpperCaseWord:Integer",
                                "NewLine",
                                "EndBlock",

                                "BeginBlock",
                                "LowerCaseWord:let",
                                "LowerCaseWord:sum3",
                                "LowerCaseWord:a",
                                "AssignOperator:=",
                                "BeginBlock",
                                "LowerCaseWord:let",
                                "LowerCaseWord:x",
                                "AssignOperator:=",
                                "Integer:3",

                                "BeginBlock",
                                "LowerCaseWord:a",
                                "Operator10:+",
                                "Integer:3",
                                "NewLine",
                                "EndBlock",

                                "NewLine",

                                "LowerCaseWord:println",
                                "Integer:10",
                                "NewLine",
                                "EndBlock",
                                "NewLine",
                                "EndBlock",
                            )
                        )
                }
            }
    }
    context("With nested expressions 3") {
        """type Int = Integer
          |
          |
          |let sum3 a = 
          | let x = 3
          |   a + 3
          |let sum a b = a = b
        """.trimMargin().kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks(endExpression)
            .apply {
                should("Should return 4 expression") {
                    asSequence()
                        .asStringSequence()
                        .toList().printTokens()
                        .shouldBe(
                            listOf(
                                "BeginBlock",
                                "LowerCaseWord:type",
                                "UpperCaseWord:Int",
                                "AssignOperator:=",
                                "UpperCaseWord:Integer",
                                "NewLine",
                                "EndBlock",

                                "BeginBlock",
                                "LowerCaseWord:let",
                                "LowerCaseWord:sum3",
                                "LowerCaseWord:a",
                                "AssignOperator:=",

                                "BeginBlock",
                                "LowerCaseWord:let",
                                "LowerCaseWord:x",
                                "AssignOperator:=",
                                "Integer:3",

                                "BeginBlock",
                                "LowerCaseWord:a",
                                "Operator10:+",
                                "Integer:3",
                                "NewLine",
                                "EndBlock",
                                "NewLine",
                                "EndBlock",
                                "NewLine",
                                "EndBlock",

                                "BeginBlock",
                                "LowerCaseWord:let",
                                "LowerCaseWord:sum",
                                "LowerCaseWord:a",
                                "LowerCaseWord:b",
                                "AssignOperator:=",
                                "LowerCaseWord:a",
                                "AssignOperator:=",
                                "LowerCaseWord:b",
                                "NewLine",
                                "EndBlock",
                            )
                        )
                }
            }
    }
    context("Check newline preservation on traits") {
        """
            trait Num =
            |    sum
            |    prod
            |    
            |    
            |    
            |    div
        """.trimMargin()
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks(endExpression)
            .apply {
                should("Should return the two new lines and one endExpression") {
                    asSequence().asStringSequence().toList().printTokens().shouldBe(
                        listOf(
                            "BeginBlock",
                            "LowerCaseWord:trait",
                            "UpperCaseWord:Num",
                            "AssignOperator:=",
                            "BeginBlock",
                            "LowerCaseWord:sum",
                            "NewLine",
                            "LowerCaseWord:prod",
                            "NewLine",
                            "LowerCaseWord:div",
                            "NewLine",
                            "EndBlock",
                            "NewLine",
                            "EndBlock"
                        )
                    )
                }
            }
    }
})

class KSharpLexerExpressionBlocks : StringSpec({
    val endExpression: (TokenType) -> LexerToken = {
        LexerToken(
            type = it,
            token = TextToken("", 0, 0)
        )
    }
    "Block Expression" {
        """
            10 +
            |   calc 10 20
        """.trimMargin()
            .also { println(it) }
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks(endExpression)
            .apply {
                asSequence().asStringSequence().toList().printTokens().shouldBe(
                    listOf(
                        "BeginBlock",
                        "Integer:10",
                        "Operator10:+",
                        "BeginBlock",
                        "LowerCaseWord:calc",
                        "Integer:10",
                        "Integer:20",
                        "NewLine",
                        "EndBlock",
                        "NewLine",
                        "EndBlock",
                    )
                )
            }
    }
    "Many Expression in block" {
        """
            10 + 
            |   calc 10 20 + 
            |   inc 2
        """.trimMargin()
            .also { println(it) }
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks(endExpression)
            .apply {
                asSequence().asStringSequence().toList().printTokens().shouldBe(
                    listOf(
                        "BeginBlock",
                        "Integer:10",
                        "Operator10:+",
                        "BeginBlock",
                        "LowerCaseWord:calc",
                        "Integer:10",
                        "Integer:20",
                        "Operator10:+",
                        "NewLine",
                        "LowerCaseWord:inc",
                        "Integer:2",
                        "NewLine",
                        "EndBlock",
                        "NewLine",
                        "EndBlock",
                    )
                )
            }
    }
    "If-then-else Expression" {
        """
            |if true 
            |   then 10 + 20 
            |   else 30 + 40
        """.trimMargin()
            .also { println(it) }
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks(endExpression)
            .apply {
                asSequence().asStringSequence().toList().printTokens().shouldBe(
                    listOf(
                        "BeginBlock",
                        "If:if",
                        "LowerCaseWord:true",
                        "BeginBlock",
                        "LowerCaseWord:then",
                        "Integer:10",
                        "Operator10:+",
                        "Integer:20",
                        "NewLine",
                        "LowerCaseWord:else",
                        "Integer:30",
                        "Operator10:+",
                        "Integer:40",
                        "NewLine",
                        "EndBlock",
                        "NewLine",
                        "EndBlock",
                    )
                )
            }
    }
    "Let Expression" {
        """
            |let x = 10
            |    y = 20
            |    z = 30
            |    x + y + z
        """.trimMargin()
            .also { println(it) }
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks(endExpression)
            .apply {
                asSequence().asStringSequence().toList().printTokens().shouldBe(
                    listOf(
                        "BeginBlock",
                        "LowerCaseWord:let",
                        "LowerCaseWord:x",
                        "AssignOperator:=",
                        "Integer:10",
                        "BeginBlock",
                        "LowerCaseWord:y",
                        "AssignOperator:=",
                        "Integer:20",
                        "NewLine",
                        "LowerCaseWord:z",
                        "AssignOperator:=",
                        "Integer:30",
                        "NewLine",
                        "LowerCaseWord:x",
                        "Operator10:+",
                        "LowerCaseWord:y",
                        "Operator10:+",
                        "LowerCaseWord:z",
                        "NewLine",
                        "EndBlock",
                        "NewLine",
                        "EndBlock",
                    )
                )
            }
    }
})