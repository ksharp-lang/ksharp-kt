package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Location
import org.ksharp.nodes.*
import org.ksharp.test.shouldBeRight

class ExpressionParserTest : StringSpec({
    "function call" {
        "sum 10 20"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                FunctionCallNode(
                    "sum",
                    FunctionType.Function,
                    listOf(
                        LiteralValueNode(
                            "10",
                            LiteralValueType.Integer,
                            Location.NoProvided
                        ),
                        LiteralValueNode(
                            "20",
                            LiteralValueType.Integer,
                            Location.NoProvided
                        )
                    ),
                    Location.NoProvided
                )
            )
    }
    "function name call" {
        "sum->two 10 20"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                FunctionCallNode(
                    "sum->two",
                    FunctionType.Function,
                    listOf(
                        LiteralValueNode(
                            "10",
                            LiteralValueType.Integer,
                            Location.NoProvided
                        ),
                        LiteralValueNode(
                            "20",
                            LiteralValueType.Integer,
                            Location.NoProvided
                        )
                    ),
                    Location.NoProvided
                )
            )
    }
    "function call receiving tuples" {
        "moveX 10,20 5"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                FunctionCallNode(
                    "moveX",
                    FunctionType.Function,
                    listOf(
                        LiteralCollectionNode(
                            listOf(
                                LiteralValueNode(
                                    "10",
                                    LiteralValueType.Integer,
                                    Location.NoProvided
                                ), LiteralValueNode(
                                    "20",
                                    LiteralValueType.Integer,
                                    Location.NoProvided
                                )
                            ),
                            LiteralCollectionType.Tuple,
                            Location.NoProvided
                        ),
                        LiteralValueNode(
                            "5",
                            LiteralValueType.Integer,
                            Location.NoProvided
                        )
                    ),
                    Location.NoProvided
                )
            )
    }
    "type instance" {
        "Point 10 20"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                FunctionCallNode(
                    "Point",
                    FunctionType.TypeInstance,
                    listOf(
                        LiteralValueNode(
                            "10",
                            LiteralValueType.Integer,
                            Location.NoProvided
                        ),
                        LiteralValueNode(
                            "20",
                            LiteralValueType.Integer,
                            Location.NoProvided
                        )
                    ),
                    Location.NoProvided
                )
            )
    }
    "operator as prefix function" {
        "(+) 10 20"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                FunctionCallNode(
                    "(+)",
                    FunctionType.Operator,
                    listOf(
                        LiteralValueNode(
                            "10",
                            LiteralValueType.Integer,
                            Location.NoProvided
                        ),
                        LiteralValueNode(
                            "20",
                            LiteralValueType.Integer,
                            Location.NoProvided
                        )
                    ),
                    Location.NoProvided
                )
            )
    }
    "function with variables" {
        "map toString [10, 20, 30]"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                FunctionCallNode(
                    "map",
                    FunctionType.Function,
                    listOf(
                        LiteralValueNode(
                            "toString",
                            LiteralValueType.Binding,
                            Location.NoProvided
                        ),
                        LiteralCollectionNode(
                            listOf(
                                LiteralValueNode(
                                    "10",
                                    LiteralValueType.Integer,
                                    Location.NoProvided
                                ),
                                LiteralValueNode(
                                    "20",
                                    LiteralValueType.Integer,
                                    Location.NoProvided
                                ),
                                LiteralValueNode(
                                    "30",
                                    LiteralValueType.Integer,
                                    Location.NoProvided
                                )
                            ), LiteralCollectionType.List, Location.NoProvided
                        )
                    ),
                    Location.NoProvided
                )
            )
    }
    "operator 12 test" {
        "10 ** 2"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "**",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided
                )
            )
    }
    "operator 11 test" {
        "10 * 2"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "*",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided
                )
            )
    }
    "operator 10 test" {
        "10 + 2"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "+",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided
                )
            )
    }
    "operator precedence test" {
        "10 + 2 * 3"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "*",
                    OperatorNode(
                        "+",
                        LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                        LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                        Location.NoProvided
                    ),
                    LiteralValueNode("3", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided
                )
            )
    }
    "operator 9 test" {
        "10 >> 2"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    ">>",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided
                )
            )
    }
    "operator 8 test" {
        "10 > 2"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    ">",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided
                )
            )
    }
    "operator 7 test" {
        "10 != 2"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "!=",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided
                )
            )
    }
    "operator 6 test" {
        "10 & 2"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "&",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided
                )
            )
    }
    "operator 5 test" {
        "10 ^ 2"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "^",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided
                )
            )
    }
    "operator 4 test" {
        "10 | 2"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "|",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided
                )
            )
    }
    "operator 3 test" {
        "10 && 2"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "&&",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided
                )
            )
    }
    "operator 2 test" {
        "10 || 2"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "||",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided
                )
            )
    }
    "operator 1 test" {
        "10 $ 2"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "$",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided
                )
            )
    }
    "operator 0 test" {
        "10 . 2"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    ".",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided
                )
            )
    }
    "operator test" {
        "10 : 2"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    ":",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided
                )
            )
    }
    "tuple and operators" {
        "10 , 2 + 1"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "+",
                    LiteralCollectionNode(
                        listOf(
                            LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                            LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided)
                        ), LiteralCollectionType.Tuple, Location.NoProvided
                    ),
                    LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided
                )
            )
    }
    "tuple and operators 2" {
        "10 , (2 + 1)"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                LiteralCollectionNode(
                    listOf(
                        LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                        OperatorNode(
                            "+",
                            LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                            LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided
                        )
                    ), LiteralCollectionType.Tuple, Location.NoProvided
                ),
            )
    }
    "custom operator precedence test" {
        "10 +> 2 * 3"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "*",
                    OperatorNode(
                        "+>",
                        LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                        LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                        Location.NoProvided
                    ),
                    LiteralValueNode("3", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided
                )
            )
    }
    "collection with expressions" {
        "[10 , 2 + 1]"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                LiteralCollectionNode(
                    listOf(
                        LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                        OperatorNode(
                            "+",
                            LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                            LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided
                        )
                    ), LiteralCollectionType.List, Location.NoProvided
                ),
            )
    }
    "map with expressions" {
        "{(10 + 2): 10 + 20, \"key2\": 20}"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                LiteralCollectionNode(
                    listOf(
                        LiteralMapEntryNode(
                            OperatorNode(
                                "+",
                                LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                                LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                                Location.NoProvided
                            ),
                            OperatorNode(
                                "+",
                                LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                                LiteralValueNode("20", LiteralValueType.Integer, Location.NoProvided),
                                Location.NoProvided
                            ), Location.NoProvided
                        ),
                        LiteralMapEntryNode(
                            LiteralValueNode("\"key2\"", LiteralValueType.String, Location.NoProvided),
                            LiteralValueNode("20", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided
                        )
                    ), LiteralCollectionType.Map, Location.NoProvided
                ),
            )
    }
    "block expressions" {
        """sum 10
           |   20
           |        30 + 15
        """.trimMargin()
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                FunctionCallNode(
                    "sum",
                    FunctionType.Function,
                    listOf(
                        LiteralValueNode(
                            "10",
                            LiteralValueType.Integer,
                            Location.NoProvided
                        ),
                        LiteralValueNode(
                            "20",
                            LiteralValueType.Integer,
                            Location.NoProvided
                        ),
                        OperatorNode(
                            "+",
                            LiteralValueNode("30", LiteralValueType.Integer, Location.NoProvided),
                            LiteralValueNode("15", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided
                        ),
                    ),
                    Location.NoProvided
                )
            )
    }
    "function and operators in block expressions" {
        """10 +
           |   sum 5
           |       30 + 15
        """.trimMargin()
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "+",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    FunctionCallNode(
                        "sum",
                        FunctionType.Function,
                        listOf(
                            LiteralValueNode(
                                "5",
                                LiteralValueType.Integer,
                                Location.NoProvided
                            ),
                            OperatorNode(
                                "+",
                                LiteralValueNode("30", LiteralValueType.Integer, Location.NoProvided),
                                LiteralValueNode("15", LiteralValueType.Integer, Location.NoProvided),
                                Location.NoProvided
                            ),
                        ),
                        Location.NoProvided
                    ),
                    Location.NoProvided
                )
            )
    }
    "function and operator expressions" {
        """sum 10 20 30 + 15
        """.trimMargin()
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "+",
                    FunctionCallNode(
                        "sum",
                        FunctionType.Function,
                        listOf(
                            LiteralValueNode(
                                "10",
                                LiteralValueType.Integer,
                                Location.NoProvided
                            ),
                            LiteralValueNode(
                                "20",
                                LiteralValueType.Integer,
                                Location.NoProvided
                            ),
                            LiteralValueNode("30", LiteralValueType.Integer, Location.NoProvided),
                        ),
                        Location.NoProvided
                    ),
                    LiteralValueNode("15", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided
                ),
            )
    }
    "complete if expression" {
        "if 4 > a then 10 else 20"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                IfNode(
                    OperatorNode(
                        ">",
                        LiteralValueNode("4", LiteralValueType.Integer, Location.NoProvided),
                        FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                        Location.NoProvided
                    ),
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("20", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided
                )
            )
    }
    "complete if without else expression" {
        "if 4 > a then 10"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                IfNode(
                    OperatorNode(
                        ">",
                        LiteralValueNode("4", LiteralValueType.Integer, Location.NoProvided),
                        FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                        Location.NoProvided
                    ),
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    UnitNode(Location.NoProvided),
                    Location.NoProvided
                )
            )
    }
    "complete if in block expression" {
        """if 4 > a 
           |    then 10
           |    else 20""".trimMargin()
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                IfNode(
                    OperatorNode(
                        ">",
                        LiteralValueNode("4", LiteralValueType.Integer, Location.NoProvided),
                        FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                        Location.NoProvided
                    ),
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("20", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided
                )
            )
    }
    "function with if expressions" {
        """sum 10
           |   if 1 != 2 
           |      then 1
           |      else 2
           |   15
        """.trimMargin()
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                FunctionCallNode(
                    "sum",
                    FunctionType.Function,
                    listOf(
                        LiteralValueNode(
                            "10",
                            LiteralValueType.Integer,
                            Location.NoProvided
                        ),
                        IfNode(
                            OperatorNode(
                                "!=",
                                LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                                LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                                Location.NoProvided
                            ),
                            LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                            LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided
                        ),
                        LiteralValueNode(
                            "15",
                            LiteralValueType.Integer,
                            Location.NoProvided
                        ),
                    ),
                    Location.NoProvided
                )
            )
    }
    "function with if without else expressions" {
        """sum 10
           |   if 1 != 2 
           |      then 1
           |   15
        """.trimMargin()
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                FunctionCallNode(
                    "sum",
                    FunctionType.Function,
                    listOf(
                        LiteralValueNode(
                            "10",
                            LiteralValueType.Integer,
                            Location.NoProvided
                        ),
                        IfNode(
                            OperatorNode(
                                "!=",
                                LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                                LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                                Location.NoProvided
                            ),
                            LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                            UnitNode(Location.NoProvided),
                            Location.NoProvided
                        ),
                        LiteralValueNode(
                            "15",
                            LiteralValueType.Integer,
                            Location.NoProvided
                        ),
                    ),
                    Location.NoProvided
                )
            )
    }
    "if with unit expression" {
        "if 4 > a then () else ()"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                IfNode(
                    OperatorNode(
                        ">",
                        LiteralValueNode("4", LiteralValueType.Integer, Location.NoProvided),
                        FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                        Location.NoProvided
                    ),
                    UnitNode(Location.NoProvided),
                    UnitNode(Location.NoProvided),
                    Location.NoProvided
                )
            )
    }
    "Type instance with labels" {
        "Username label1: x label2: 20"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                FunctionCallNode(
                    "Username",
                    FunctionType.TypeInstance,
                    listOf(
                        LiteralValueNode("label1:", LiteralValueType.Label, Location.NoProvided),
                        LiteralValueNode("x", LiteralValueType.Binding, Location.NoProvided),
                        LiteralValueNode("label2:", LiteralValueType.Label, Location.NoProvided),
                        LiteralValueNode("20", LiteralValueType.Integer, Location.NoProvided),
                    ),
                    Location.NoProvided
                )
            )
    }
})