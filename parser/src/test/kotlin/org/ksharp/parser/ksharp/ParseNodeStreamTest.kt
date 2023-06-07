package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.Line
import org.ksharp.common.Location
import org.ksharp.common.Offset
import org.ksharp.nodes.*

class ParseNodeStreamTest : StringSpec({
    "Parse invalid tokens" {
        "1 +"
            .parseModuleAsNodeSequence()
            .shouldBe(
                listOf(
                    InvalidNode(
                        token = listOf(
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
            .onEach { println(it) }
            .shouldBe(
                listOf(
                    InvalidNode(
                        token = listOf(
                            InvalidToken(
                                text = "import",
                                type = KSharpTokenType.LowerCaseWord,
                                location = Location(
                                    start = (Line(value = 1) to Offset(value = 0)),
                                    end = (Line(value = 1) to Offset(value = 6))
                                )
                            ),
                            InvalidToken(
                                text = "math",
                                type = KSharpTokenType.LowerCaseWord,
                                location = Location(
                                    start = (Line(value = 1) to Offset(value = 7)),
                                    end = (Line(value = 1) to Offset(value = 11))
                                )
                            ),
                            InvalidToken(
                                text = "as",
                                type = KSharpTokenType.LowerCaseWord,
                                location = Location(
                                    start = (Line(value = 1) to Offset(value = 12)),
                                    end = (Line(value = 1) to Offset(value = 14))
                                )
                            ),
                            InvalidToken(
                                text = "k",
                                type = KSharpTokenType.LowerCaseWord,
                                location = Location(
                                    start = (Line(value = 1) to Offset(value = 15)),
                                    end = (Line(value = 1) to Offset(value = 16))
                                )
                            ),
                            InvalidToken(
                                text = "",
                                type = KSharpTokenType.BeginBlock,
                                location = Location(
                                    start = (Line(value = 0) to Offset(value = 0)),
                                    end = (Line(value = 0) to Offset(value = 0))
                                )
                            ),
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
})
