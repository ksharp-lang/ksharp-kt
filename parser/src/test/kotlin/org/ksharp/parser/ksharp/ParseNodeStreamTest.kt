package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.Line
import org.ksharp.common.Location
import org.ksharp.common.Offset
import org.ksharp.common.new
import org.ksharp.nodes.*
import org.ksharp.parser.BaseParserErrorCode

private fun List<NodeData>.asStringList() =
    map {
        val nodeType = it.javaClass.simpleName
        val suffix = if (it is InvalidNode)
            " ${it.tokens.joinToString("") { s -> s.text }}"
        else ""
        "$nodeType$suffix"
    }

class ParseNodeStreamTest : StringSpec({
    "Parse invalid tokens" {
        "1 +"
            .parseModuleAsNodeSequence()
            .shouldBe(
                listOf(
                    InvalidNode(
                        error = BaseParserErrorCode.ExpectingToken.new(
                            Location(
                                Line(value = 1) to Offset(value = 0),
                                Line(value = 1) to Offset(value = 1)
                            ),
                            "token" to "<LowerCaseWord, Operator different internal, type>",
                            "received-token" to "Integer:1"
                        ),
                        tokens = listOf(
                            InvalidToken(
                                "1",
                                KSharpTokenType.Integer,
                                Location(Line(value = 1) to Offset(value = 0), Line(value = 1) to Offset(value = 1))
                            ),
                            InvalidToken(
                                "+",
                                KSharpTokenType.Operator10,
                                Location(Line(value = 1) to Offset(value = 2), Line(value = 1) to Offset(value = 3))
                            )
                        )
                    )
                )
            )
    }
    "Parse tokens" {
        """import math as k
           |   1 +
           |sum a = a + b
        """.trimMargin()
            .parseModuleAsNodeSequence()
            .shouldBe(
                listOf(
                    ImportNode(
                        moduleName = "math", key = "k", location = Location(
                            start = (Line(value = 1) to Offset(value = 0)),
                            end = (Line(value = 1) to Offset(value = 6))
                        ),
                        locations = ImportNodeLocations(
                            importLocation = Location(
                                start = (Line(value = 1) to Offset(value = 0)),
                                end = (Line(value = 1) to Offset(value = 6))
                            ),
                            moduleNameBegin = Location(
                                start = (Line(value = 1) to Offset(value = 7)),
                                end = (Line(value = 1) to Offset(value = 11))
                            ),
                            moduleNameEnd = Location(
                                start = (Line(value = 1) to Offset(value = 7)),
                                end = (Line(value = 1) to Offset(value = 11))
                            ),
                            asLocation = Location(
                                start = (Line(value = 1) to Offset(value = 12)),
                                end = (Line(value = 1) to Offset(value = 14))
                            ),
                            keyLocation = Location(
                                start = (Line(value = 1) to Offset(value = 15)),
                                end = (Line(value = 1) to Offset(value = 16))
                            )
                        )
                    ),
                    InvalidNode(
                        error = BaseParserErrorCode.ExpectingToken.new(
                            Location(
                                start = (Line(value = 2) to Offset(value = 3)),
                                end = (Line(value = 2) to Offset(value = 4))
                            ),
                            "token" to "<LowerCaseWord, Operator different internal, type>",
                            "received-token" to "Integer:1"
                        ),
                        tokens = listOf(
                            InvalidToken(
                                text = "1",
                                type = KSharpTokenType.Integer,
                                location = Location(
                                    start = (Line(value = 2) to Offset(value = 3)),
                                    end = (Line(value = 2) to Offset(value = 4))
                                )
                            ),
                            InvalidToken(
                                text = "+",
                                type = KSharpTokenType.Operator10,
                                location = Location(
                                    start = (Line(value = 2) to Offset(value = 5)),
                                    end = (Line(value = 2) to Offset(value = 6))
                                )
                            )
                        )
                    ),

                    FunctionNode(
                        native = false, pub = false, annotations = null, name = "sum", parameters = listOf("a"),
                        expression = OperatorNode(
                            operator = "+",
                            left = FunctionCallNode(
                                name = "a",
                                type = FunctionType.Function,
                                arguments = listOf(),
                                location = Location(
                                    start = (Line(value = 3) to Offset(value = 8)),
                                    end = (Line(value = 3) to Offset(value = 9))
                                )
                            ),
                            right = FunctionCallNode(
                                name = "b",
                                type = FunctionType.Function,
                                arguments = listOf(),
                                location = Location(
                                    start = (Line(value = 3) to Offset(value = 12)),
                                    end = (Line(value = 3) to Offset(value = 13))
                                )
                            ),
                            location = Location(
                                start = (Line(value = 3) to Offset(value = 10)),
                                end = (Line(value = 3) to Offset(value = 11))
                            )
                        ),
                        location = Location(
                            start = (Line(value = 3) to Offset(value = 0)),
                            end = (Line(value = 3) to Offset(value = 3))
                        ),
                        locations = FunctionNodeLocations(
                            nativeLocation = Location.NoProvided,
                            pubLocation = Location.NoProvided,
                            name = Location(
                                start = (Line(value = 3) to Offset(value = 0)),
                                end = (Line(value = 3) to Offset(value = 3))
                            ),
                            parameters = listOf(
                                Location(
                                    start = (Line(value = 3) to Offset(value = 4)),
                                    end = (Line(value = 3) to Offset(value = 5))
                                )
                            ),
                            assignOperator = Location(
                                start = (Line(value = 3) to Offset(value = 6)),
                                end = (Line(value = 3) to Offset(value = 7))
                            )
                        )
                    )

                )
            )
    }
    "Check produce node types" {
        """import math as k
           |   1 +
           |        3 + 5
           |sum a = a + b
        """.trimMargin()
            .parseModuleAsNodeSequence()
            .asStringList()
            .shouldBe(listOf("ImportNode", "InvalidNode 1+3+5", "FunctionNode"))
    }
    "Check produce node types 2" {
        """import math as k
           |   1 +
           |3 + 5
           |sum a = a + b
        """.trimMargin()
            .parseModuleAsNodeSequence()
            .asStringList()
            .shouldBe(listOf("ImportNode", "InvalidNode 1+", "InvalidNode 3+5", "FunctionNode"))
    }
})
