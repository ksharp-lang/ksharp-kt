package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.sequences.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.ksharp.common.Line
import org.ksharp.common.Offset
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
                                TextToken("type", 0, 4)
                            ),
                            LexerToken(
                                KSharpTokenType.WhiteSpace,
                                TextToken(" ", 4, 5)
                            ),
                            LexerToken(
                                KSharpTokenType.UpperCaseWord,
                                TextToken("Name", 5, 9)
                            ),
                            LexerToken(
                                KSharpTokenType.WhiteSpace,
                                TextToken(" ", 9, 10)
                            ),
                            LexerToken(
                                KSharpTokenType.Label,
                                TextToken("lbl:", 10, 14)
                            ),
                            LexerToken(
                                KSharpTokenType.WhiteSpace,
                                TextToken(" ", 14, 15)
                            ),
                            LexerToken(
                                KSharpTokenType.UpperCaseWord,
                                TextToken("Name", 15, 19)
                            ),
                            LexerToken(
                                KSharpTokenType.Operator,
                                TextToken(":", 19, 20)
                            ),
                            LexerToken(
                                type = KSharpTokenType.WhiteSpace,
                                token = TextToken(text = " ", startOffset = 20, endOffset = 21)
                            ),
                            LexerToken(
                                KSharpTokenType.UpperCaseWord,
                                TextToken("User_name", 21, 30)
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
                LexerToken(KSharpTokenType.OpenBracket, TextToken("[", 0, 1)),
                LexerToken(KSharpTokenType.CloseBracket, TextToken("]", 1, 2)),
                LexerToken(KSharpTokenType.OpenParenthesis, TextToken("(", 2, 3)),
                LexerToken(KSharpTokenType.CloseParenthesis, TextToken(")", 3, 4)),
                LexerToken(KSharpTokenType.OpenCurlyBraces, TextToken("{", 4, 5)),
                LexerToken(KSharpTokenType.CloseCurlyBraces, TextToken("}", 5, 6)),
                LexerToken(KSharpTokenType.Alt, TextToken("@", 6, 7)),
                LexerToken(KSharpTokenType.Comma, TextToken(",", 7, 8))
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
                    token = TextToken(text = "(+)", startOffset = 0, endOffset = 3)
                ),
                LexerToken(
                    type = KSharpTokenType.OpenParenthesis,
                    token = TextToken(text = "(", startOffset = 4, endOffset = 5)
                ),
                LexerToken(
                    type = KSharpTokenType.Integer,
                    token = TextToken(text = "1", startOffset = 5, endOffset = 6)
                )
            )
    }
    "Given lexer, check #( #+ #{, #[" {
        "#( #+ #{, #[".kSharpLexer()
            .asSequence()
            .toList()
            .onEach(::println)
            .shouldContainAll(
                LexerToken(KSharpTokenType.Operator, TextToken("#", 0, 1)),
                LexerToken(KSharpTokenType.OpenParenthesis, TextToken("(", 1, 2)),
                LexerToken(KSharpTokenType.Operator, TextToken("#+", 3, 5)),
                LexerToken(KSharpTokenType.Operator, TextToken("#", 6, 7)),
                LexerToken(KSharpTokenType.OpenCurlyBraces, TextToken("{", 7, 8)),
                LexerToken(KSharpTokenType.OpenSetBracket, TextToken("#[", 10, 12)),
            )
    }
    "Given lexer, check operators" {
        "+-*/%><=!&$#^?.\\|".kSharpLexer()
            .asSequence()
            .toList()
            .shouldContainAll(
                LexerToken(KSharpTokenType.Operator, TextToken("+-*/%><=!&$#^?.\\|", 0, 17)),
            )
    }
    "Given lexer, check decimal" {
        "1.6"
            .kSharpLexer()
            .asSequence()
            .toList().onEach(::println).shouldContainAll(
                LexerToken(KSharpTokenType.Float, TextToken("1.6", 0, 3))
            )
    }
    "Given lexer, check integers, decimals, integer and dot operator" {
        "100 1.3 .6 2. 1_000 0xFFFFbB 0b110011 0o12345 010 ".kSharpLexer()
            .asSequence()
            .toList().onEach(::println)
            .shouldContainAll(
                LexerToken(KSharpTokenType.Integer, TextToken("100", 0, 3)),
                LexerToken(KSharpTokenType.Float, TextToken("1.3", 4, 7)),
                LexerToken(KSharpTokenType.Float, TextToken(".6", 8, 10)),
                LexerToken(KSharpTokenType.Integer, TextToken("2", 11, 12)),
                LexerToken(KSharpTokenType.Operator, TextToken(".", 12, 13)),
                LexerToken(KSharpTokenType.Integer, TextToken("1_000", 14, 19)),
                LexerToken(KSharpTokenType.HexInteger, TextToken("0xFFFFbB", 20, 28)),
                LexerToken(
                    KSharpTokenType.BinaryInteger,
                    TextToken(text = "0b110011", startOffset = 29, endOffset = 37)
                ),
                LexerToken(
                    KSharpTokenType.OctalInteger,
                    TextToken(text = "0o12345", startOffset = 38, endOffset = 45)
                ),
                LexerToken(
                    KSharpTokenType.Integer,
                    TextToken(text = "010", startOffset = 46, endOffset = 49)
                )
            )
    }
    "Given lexer, check hex, binary, octal" {
        "0xFF0F".kSharpLexer()
            .asSequence()
            .toList().onEach(::println)
            .shouldContainAll(
                LexerToken(KSharpTokenType.HexInteger, TextToken("0xFF0F", 0, 6)),
            )

        "0xFF_FF".kSharpLexer()
            .asSequence()
            .toList().onEach(::println)
            .shouldContainAll(
                LexerToken(KSharpTokenType.HexInteger, TextToken("0xFF_FF", 0, 7)),
            )
        "0b0001_1001".kSharpLexer()
            .asSequence()
            .toList().onEach(::println)
            .shouldContainAll(
                LexerToken(KSharpTokenType.BinaryInteger, TextToken("0b0001_1001", 0, 11)),
            )
        "0b0011".kSharpLexer()
            .asSequence()
            .toList().onEach(::println)
            .shouldContainAll(
                LexerToken(KSharpTokenType.BinaryInteger, TextToken("0b0011", 0, 6)),
            )
        "0o0011_6471".kSharpLexer()
            .asSequence()
            .toList().onEach(::println)
            .shouldContainAll(
                LexerToken(KSharpTokenType.OctalInteger, TextToken("0o0011_6471", 0, 11)),
            )
        "0o1573".kSharpLexer()
            .asSequence()
            .toList().onEach(::println)
            .shouldContainAll(
                LexerToken(KSharpTokenType.OctalInteger, TextToken("0o1573", 0, 6)),
            )
    }
    "Given a lexer, check collapse tokens, should remove whitespace only" {
        "import ksharp .test as math".kSharpLexer()
            .enableLookAhead()
            .collapseKSharpTokens()
            .asSequence()
            .toList()
            .shouldContainAll(
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("import", 0, 6)),
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("ksharp", 7, 13)),
                LexerToken(KSharpTokenType.Operator0, TextToken(".", 14, 15)),
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("test", 15, 19)),
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("as", 20, 22)),
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("math", 23, 27))
            )
    }
    "Given a lexer, check collapse tokens to form function tokens" {
        "internal->wire.name  ->  wire data.list map . test".kSharpLexer()
            .enableLookAhead()
            .collapseKSharpTokens()
            .asSequence()
            .toList().onEach(::println)
            .shouldContainAll(
                LexerToken(KSharpTokenType.FunctionName, TextToken("internal->wire", 0, 14)),
                LexerToken(KSharpTokenType.Operator0, TextToken(".", 14, 15)),
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("name", 15, 19)),
                LexerToken(KSharpTokenType.Operator10, TextToken("->", 21, 23)),
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("wire", 25, 29)),
                LexerToken(KSharpTokenType.FunctionName, TextToken("data.list", 30, 39)),
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("map", 40, 43)),
                LexerToken(KSharpTokenType.Operator0, TextToken(".", 44, 45)),
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("test", 46, 50)),
            )
    }
    "Given a lexer, check collapse tokens, should leave really important whitespaces (those after a newline) inside the NewLine token" {
        "internal->wire.name = \n    10".kSharpLexer()
            .ensureNewLineAtEnd()
            .enableLookAhead()
            .collapseKSharpTokens()
            .asSequence()
            .toList().onEach { println("+++ $it") }
            .shouldContainAll(
                LexerToken(KSharpTokenType.FunctionName, TextToken("internal->wire", 0, 14)),
                LexerToken(KSharpTokenType.Operator0, TextToken(".", 14, 15)),
                LexerToken(KSharpTokenType.LowerCaseWord, TextToken("name", 15, 19)),
                LexerToken(KSharpTokenType.AssignOperator, TextToken("=", 20, 21)),
                LexerToken(KSharpTokenType.NewLine, TextToken("\n    ", 22, 27)),
                LexerToken(KSharpTokenType.Integer, TextToken("10", 27, 29)),
                LexerToken(KSharpTokenType.NewLine, TextToken("\n", 30, 30)),
            )
    }
    "Given a lexer, map operators" {
        "** *>> //> %%% +++ - << >> <== != & ||| ^& && || = . # $ ? :".kSharpLexer()
            .ensureNewLineAtEnd()
            .enableLookAhead()
            .collapseKSharpTokens()
            .asSequence()
            .toList()
            .printTokens()
            .shouldBe(
                listOf(
                    LexerToken(
                        type = KSharpTokenType.Operator12,
                        token = TextToken(text = "**", startOffset = 0, endOffset = 2)
                    ),

                    LexerToken(
                        type = KSharpTokenType.Operator11,
                        token = TextToken(text = "*>>", startOffset = 3, endOffset = 6)
                    ),
                    LexerToken(
                        type = KSharpTokenType.Operator11,
                        token = TextToken(text = "//>", startOffset = 7, endOffset = 10)
                    ),
                    LexerToken(
                        type = KSharpTokenType.Operator11,
                        token = TextToken(text = "%%%", startOffset = 11, endOffset = 14)
                    ),
                    LexerToken(
                        type = KSharpTokenType.Operator10,
                        token = TextToken(text = "+++", startOffset = 15, endOffset = 18)
                    ),
                    LexerToken(
                        type = KSharpTokenType.Operator10,
                        token = TextToken(text = "-", startOffset = 19, endOffset = 20)
                    ),
                    LexerToken(
                        type = KSharpTokenType.Operator9,
                        token = TextToken(text = "<<", startOffset = 21, endOffset = 23)
                    ),
                    LexerToken(
                        type = KSharpTokenType.Operator9,
                        token = TextToken(text = ">>", startOffset = 24, endOffset = 26)
                    ),
                    LexerToken(
                        type = KSharpTokenType.Operator8,
                        token = TextToken(text = "<==", startOffset = 27, endOffset = 30)
                    ),
                    LexerToken(
                        type = KSharpTokenType.Operator7,
                        token = TextToken(text = "!=", startOffset = 31, endOffset = 33)
                    ),
                    LexerToken(
                        type = KSharpTokenType.Operator6,
                        token = TextToken(text = "&", startOffset = 34, endOffset = 35)
                    ),
                    LexerToken(
                        type = KSharpTokenType.Operator4,
                        token = TextToken(text = "|||", startOffset = 36, endOffset = 39)
                    ),
                    LexerToken(
                        type = KSharpTokenType.Operator5,
                        token = TextToken(text = "^&", startOffset = 40, endOffset = 42)
                    ),
                    LexerToken(
                        type = KSharpTokenType.Operator3,
                        token = TextToken(text = "&&", startOffset = 43, endOffset = 45)
                    ),
                    LexerToken(
                        type = KSharpTokenType.Operator2,
                        token = TextToken(text = "||", startOffset = 46, endOffset = 48)
                    ),
                    LexerToken(
                        type = KSharpTokenType.AssignOperator,
                        token = TextToken(text = "=", startOffset = 49, endOffset = 50)
                    ),
                    LexerToken(
                        type = KSharpTokenType.Operator0,
                        token = TextToken(text = ".", startOffset = 51, endOffset = 52)
                    ),
                    LexerToken(
                        type = KSharpTokenType.Operator,
                        token = TextToken(text = "#", startOffset = 53, endOffset = 54)
                    ),
                    LexerToken(
                        type = KSharpTokenType.Operator1,
                        token = TextToken(text = "$", startOffset = 55, endOffset = 56)
                    ),
                    LexerToken(
                        type = KSharpTokenType.Operator,
                        token = TextToken(text = "?", startOffset = 57, endOffset = 58)
                    ),
                    LexerToken(
                        type = KSharpTokenType.Operator,
                        token = TextToken(text = ":", startOffset = 59, endOffset = 60)
                    ),
                    LexerToken(
                        type = KSharpTokenType.NewLine,
                        token = TextToken(text = "\n", startOffset = 61, endOffset = 61)
                    )
                )
            )
    }
    "Given a lexer, map character and string" {
        "'a' \"Hello World\" \"\"\"Hello\nWorld\"\"\" \"\" '\\\''  \"\\\"\""
            .kSharpLexer()
            .ensureNewLineAtEnd()
            .enableLookAhead()
            .collapseKSharpTokens()
            .asSequence()
            .toList()
            .onEach(::println)
            .shouldBe(
                listOf(
                    LexerToken(
                        type = KSharpTokenType.Character,
                        token = TextToken(text = "'a'", startOffset = 0, endOffset = 3)
                    ),
                    LexerToken(
                        type = KSharpTokenType.String,
                        token = TextToken(text = "\"Hello World\"", startOffset = 4, endOffset = 17)
                    ),
                    LexerToken(
                        type = KSharpTokenType.MultiLineString,
                        token = TextToken(text = "\"\"\"Hello\nWorld\"\"\"", startOffset = 18, endOffset = 35)
                    ),
                    LexerToken(
                        type = KSharpTokenType.String,
                        token = TextToken(text = "\"\"", startOffset = 36, endOffset = 38)
                    ),
                    LexerToken(
                        type = KSharpTokenType.Character,
                        token = TextToken(text = "'\\''", startOffset = 39, endOffset = 43)
                    ),
                    LexerToken(
                        type = KSharpTokenType.String,
                        token = TextToken(text = "\"\\\"\"", startOffset = 45, endOffset = 49)
                    ),
                    LexerToken(
                        type = KSharpTokenType.NewLine,
                        token = TextToken(text = "\n", startOffset = 50, endOffset = 50)
                    ),
                )
            )
    }
    "Given a lexer, map invalid characters" {
        "'".kSharpLexer().ensureNewLineAtEnd().enableLookAhead().collapseKSharpTokens().asSequence().toList()
            .shouldBe(
                listOf(
                    LexerToken(
                        type = KSharpTokenType.Character,
                        token = TextToken(text = "'", startOffset = 0, endOffset = 1)
                    ),
                    LexerToken(
                        type = KSharpTokenType.NewLine,
                        token = TextToken(text = "\n", startOffset = 2, endOffset = 2)
                    ),
                )
            )

        "'a".kSharpLexer().ensureNewLineAtEnd()
            .enableLookAhead().collapseKSharpTokens().asSequence().toList()
            .shouldBe(
                listOf(
                    LexerToken(
                        type = KSharpTokenType.Character,
                        token = TextToken(text = "'a", startOffset = 0, endOffset = 2)
                    ),
                    LexerToken(
                        type = KSharpTokenType.NewLine,
                        token = TextToken(text = "\n", startOffset = 3, endOffset = 3)
                    ),
                )
            )
    }
    "Given a lexer, map invalid strings" {
        "\"".kSharpLexer().ensureNewLineAtEnd().enableLookAhead().collapseKSharpTokens().asSequence().toList()
            .shouldBe(
                listOf(
                    LexerToken(
                        type = KSharpTokenType.String,
                        token = TextToken(text = "\"", startOffset = 0, endOffset = 1)
                    ),
                    LexerToken(
                        type = KSharpTokenType.NewLine,
                        token = TextToken(text = "\n", startOffset = 2, endOffset = 2)
                    ),
                )
            )

        "\"a".kSharpLexer().ensureNewLineAtEnd().enableLookAhead().collapseKSharpTokens().asSequence().toList()
            .shouldBe(
                listOf(
                    LexerToken(
                        type = KSharpTokenType.String,
                        token = TextToken(text = "\"a", startOffset = 0, endOffset = 2)
                    ),
                    LexerToken(
                        type = KSharpTokenType.NewLine,
                        token = TextToken(text = "\n", startOffset = 3, endOffset = 3)
                    ),
                )
            )

        "\"\"\"".kSharpLexer().ensureNewLineAtEnd().enableLookAhead().collapseKSharpTokens().asSequence().toList()
            .shouldBe(
                listOf(
                    LexerToken(
                        type = KSharpTokenType.MultiLineString,
                        token = TextToken(text = "\"\"\"", startOffset = 0, endOffset = 3)
                    ),
                    LexerToken(
                        type = KSharpTokenType.NewLine,
                        token = TextToken(text = "\n", startOffset = 4, endOffset = 4)
                    ),
                )
            )
    }
    "Given a lexer, map tabs as whitespaces" {
        "\t".kSharpLexer()
            .asSequence()
            .toList()
            .shouldBe(listOf(LexerToken(KSharpTokenType.WhiteSpace, TextToken("\t", 0, 1))))
    }
    "Given a lexer, check carrier return as NewLine token" {
        "\r".kSharpLexer()
            .asSequence()
            .toList()
            .shouldBe(
                listOf(
                    LexerToken(
                        type = KSharpTokenType.NewLine,
                        token = TextToken(text = "\r", startOffset = 0, endOffset = 1)
                    ),
                )
            )
    }
    "Given a lexer, unit value" {
        "() ( 1 )"
            .kSharpLexer()
            .enableLookAhead()
            .collapseKSharpTokens()
            .asSequence().toList()
            .shouldContainAll(
                LexerToken(
                    type = KSharpTokenType.UnitValue,
                    token = TextToken(text = "()", startOffset = 0, endOffset = 2)
                ),
                LexerToken(
                    type = KSharpTokenType.OpenParenthesis,
                    token = TextToken(text = "(", startOffset = 3, endOffset = 4)
                ),
                LexerToken(
                    type = KSharpTokenType.Integer,
                    token = TextToken(text = "1", startOffset = 5, endOffset = 6)
                ),
                LexerToken(
                    type = KSharpTokenType.CloseParenthesis,
                    token = TextToken(text = ")", startOffset = 7, endOffset = 8)
                ),
            )
    }
    "if then else mapIfThenKeyword disabled" {
        "if then else"
            .kSharpLexer()
            .enableLookAhead()
            .collapseKSharpTokens()
            .asSequence().toList()
            .shouldContainAll(
                LexerToken(
                    type = KSharpTokenType.If,
                    token = TextToken(text = "if", startOffset = 0, endOffset = 2)
                ),
                LexerToken(
                    type = KSharpTokenType.LowerCaseWord,
                    token = TextToken(text = "then", startOffset = 3, endOffset = 7)
                ),
                LexerToken(
                    type = KSharpTokenType.LowerCaseWord,
                    token = TextToken(text = "else", startOffset = 8, endOffset = 12)
                )
            )
    }
    "if then else mapIfThenKeyword enabled" {
        "if then else"
            .kSharpLexer()
            .enableLookAhead()
            .collapseKSharpTokens()
            .enableMapElseThenKeywords {
                it.asSequence().toList()
            }
            .shouldContainAll(
                LexerToken(
                    type = KSharpTokenType.If,
                    token = TextToken(text = "if", startOffset = 0, endOffset = 2)
                ),
                LexerToken(
                    type = KSharpTokenType.Then,
                    token = TextToken(text = "then", startOffset = 3, endOffset = 7)
                ),
                LexerToken(
                    type = KSharpTokenType.Else,
                    token = TextToken(text = "else", startOffset = 8, endOffset = 12)
                )
            )
    }
    "let then  mapLetThenKeyword enabled" {
        "let then"
            .kSharpLexer()
            .enableLookAhead()
            .collapseKSharpTokens()
            .enableMapThenKeywords {
                it.asSequence().toList()
            }
            .shouldContainAll(
                LexerToken(
                    type = KSharpTokenType.Let,
                    token = TextToken(text = "let", startOffset = 0, endOffset = 3)
                ),
                LexerToken(
                    type = KSharpTokenType.Then,
                    token = TextToken(text = "then", startOffset = 4, endOffset = 8)
                )
            )
    }
    "let then  mapLetThenKeyword disabled" {
        "let then"
            .kSharpLexer()
            .enableLookAhead()
            .collapseKSharpTokens()
            .asSequence().toList()
            .shouldContainAll(
                LexerToken(
                    type = KSharpTokenType.Let,
                    token = TextToken(text = "let", startOffset = 0, endOffset = 3)
                ),
                LexerToken(
                    type = KSharpTokenType.LowerCaseWord,
                    token = TextToken(text = "then", startOffset = 4, endOffset = 8)
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

private fun Sequence<Token>.asLspPositionsSequence() =
    filterIsInstance<LogicalLexerToken>()
        .map {
            when (it.type) {
                KSharpTokenType.BeginBlock -> null
                KSharpTokenType.EndBlock -> null
                KSharpTokenType.NewLine -> null
                else -> "${it.text}:${it.startPosition.first.value}:${it.startPosition.second.value}"
            }
        }.filterNotNull()

private fun <T> List<T>.printTokens() = onEach { println("\"$it\",") }


private fun TokenLexerIterator<KSharpLexerState>.prepareLexerForMarkBlockTests(): KSharpLexerIterator {
    val endExpression: (TokenType) -> LexerToken = {
        LexerToken(
            type = it,
            token = TextToken("", 0, 0)
        )
    }
    return ensureNewLineAtEnd()
        .markBlocks(endExpression)
        .enableLookAhead()
        .collapseKSharpTokens()
        .discardBlocksOrNewLineTokens()
}

class KSharpLexerMarkBlocksTest : ShouldSpec({
    context("With just one block without new line") {
        "type Int = Integer"
            .kSharpLexer()
            .prepareLexerForMarkBlockTests()
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
            .prepareLexerForMarkBlockTests()
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
            .prepareLexerForMarkBlockTests()
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
            .prepareLexerForMarkBlockTests()
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
            .prepareLexerForMarkBlockTests()
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
            .prepareLexerForMarkBlockTests()
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
                                "Let:let",
                                "LowerCaseWord:sum3",
                                "LowerCaseWord:a",
                                "AssignOperator:=",
                                "BeginBlock",
                                "Let:let",
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
            .prepareLexerForMarkBlockTests()
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
                                "Let:let",
                                "LowerCaseWord:sum3",
                                "LowerCaseWord:a",
                                "AssignOperator:=",
                                "BeginBlock",
                                "Let:let",
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
            .prepareLexerForMarkBlockTests()
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
                                "Let:let",
                                "LowerCaseWord:sum3",
                                "LowerCaseWord:a",
                                "AssignOperator:=",

                                "BeginBlock",
                                "Let:let",
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
                                "Let:let",
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
            .prepareLexerForMarkBlockTests()
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
    "Block Expression" {
        """
            10 +
            |   calc 10 20
        """.trimMargin()
            .kSharpLexer()
            .prepareLexerForMarkBlockTests()
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
            .kSharpLexer()
            .prepareLexerForMarkBlockTests()
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
            .kSharpLexer()
            .prepareLexerForMarkBlockTests()
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
            .kSharpLexer()
            .prepareLexerForMarkBlockTests()
            .apply {
                asSequence().asStringSequence().toList().printTokens().shouldBe(
                    listOf(
                        "BeginBlock",
                        "Let:let",
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


class KSharpLexerNewLineLSPTest : StringSpec({
    "Check text produce line positions complaint with LSP" {
        "type Num \n\nsum a = a * 2\n   1\n"
            .lexerModule(true)
            .asSequence()
            .asLspPositionsSequence()
            .toList()
            .printTokens()
            .shouldBe(
                listOf(
                    "type:1:0",
                    "Num:1:5",
                    "sum:3:0",
                    "a:3:4",
                    "=:3:6",
                    "a:3:8",
                    "*:3:10",
                    "2:3:12",
                    "1:4:3"
                )
            )
    }
    "Check text produce line positions complaint with LSP using carrier return" {
        "type Num \n\rsum a = a * 2\r\n   1\n"
            .lexerModule(true)
            .asSequence()
            .asLspPositionsSequence()
            .toList()
            .printTokens()
            .shouldBe(
                listOf(
                    "type:1:0",
                    "Num:1:5",
                    "sum:3:0",
                    "a:3:4",
                    "=:3:6",
                    "a:3:8",
                    "*:3:10",
                    "2:3:12",
                    "1:4:3"
                )
            )
    }
    "Text unicode 16 character offsets" {
        "\"a𐐀b\""
            .lexerModule(true)
            .asSequence()
            .toList()
            .printTokens()
            .shouldContainAll(
                LogicalLexerToken(
                    token = LexerToken(
                        type = KSharpTokenType.String,
                        token = TextToken(text = "\"a𐐀b\"", startOffset = 0, endOffset = 6)
                    ),
                    startPosition = (Line(value = 1) to Offset(value = 0)),
                    endPosition = (Line(value = 1) to Offset(value = 6))
                )
            )
    }
})
