package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Location
import org.ksharp.nodes.*
import org.ksharp.parser.TokenLexerIterator
import org.ksharp.parser.collapseNewLines
import org.ksharp.parser.enableLookAhead
import org.ksharp.parser.excludeIgnoreNewLineTokens
import org.ksharp.test.shouldBeRight

private fun TokenLexerIterator<KSharpLexerState>.prepareLexerForExpressionParsing() =
    filterAndCollapseTokens()
        .excludeIgnoreNewLineTokens()
        .collapseNewLines()
        .enableLookAhead()
        .enableIndentationOffset()

class ExpressionParserTest : StringSpec({
    "function call" {
        "sum 10 20"
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
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
            .prepareLexerForExpressionParsing()
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
            .prepareLexerForExpressionParsing()
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
                            Location.NoProvided,
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
    "function call from a imported module" {
        "pos.moveX 10,20 5"
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                FunctionCallNode(
                    "pos.moveX",
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
                            Location.NoProvided,
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
    "function call without arguments" {
        "pos.moveX"
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                FunctionCallNode(
                    "pos.moveX",
                    FunctionType.Function,
                    listOf(),
                    Location.NoProvided
                )
            )
    }
    "type instance" {
        "Point 10 20"
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
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
            .prepareLexerForExpressionParsing()
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
            .prepareLexerForExpressionParsing()
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
                            ),
                            LiteralCollectionType.List, Location.NoProvided,
                        )
                    ),
                    Location.NoProvided
                )
            )
    }
    "operator 12 test" {
        "10 ** 2"
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "Operator12",
                    "**",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided,
                )
            )
    }
    "operator 11 test" {
        "10 * 2"
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "Operator11",
                    "*",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided,
                )
            )
    }
    "operator 10 test" {
        "10 + 2"
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "Operator10",
                    "+",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided,
                )
            )
    }
    "operator precedence test" {
        "10 + 2 * 3"
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "Operator10",
                    "+",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    OperatorNode(
                        "Operator11",
                        "*",
                        LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                        LiteralValueNode("3", LiteralValueType.Integer, Location.NoProvided),
                        Location.NoProvided,
                    ),
                    Location.NoProvided,
                )
            )
    }
    "operator 9 test" {
        "10 >> 2"
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "Operator9",
                    ">>",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided,
                )
            )
    }
    "operator 8 test" {
        "10 > 2"
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "Operator8",
                    ">",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided,
                )
            )
    }
    "operator 7 test" {
        "10 != 2"
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "Operator7",
                    "!=",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided,
                )
            )
    }
    "operator 6 test" {
        "10 & 2"
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "Operator6",
                    "&",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided,
                )
            )
    }
    "operator 5 test" {
        "10 ^ 2"
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "Operator5",
                    "^",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided,
                )
            )
    }
    "operator 4 test" {
        "10 | 2"
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "Operator4",
                    "|",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided,
                )
            )
    }
    "operator 3 test" {
        "10 && 2"
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "Operator3",
                    "&&",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided,
                )
            )
    }
    "operator 2 test" {
        "10 || 2"
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "Operator2",
                    "||",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided,
                )
            )
    }
    "operator 1 test" {
        "10 $ 2"
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "Operator1",
                    "$",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided,
                )
            )
    }
    "operator 0 test" {
        "10 . 2"
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "Operator0",
                    ".",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided,
                )
            )
    }
    "operator test" {
        "10 : 2"
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "Operator",
                    ":",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided,
                )
            )
    }
    "tuple and operators" {
        "10 , 2 + 1"
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "Operator10",
                    "+",
                    LiteralCollectionNode(
                        listOf(
                            LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                            LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided)
                        ),
                        LiteralCollectionType.Tuple, Location.NoProvided,
                    ),
                    LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided,
                )
            )
    }
    "tuple and operators 2" {
        "10 , (2 + 1)"
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                LiteralCollectionNode(
                    listOf(
                        LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                        OperatorNode(
                            "Operator10",
                            "+",
                            LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                            LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided,
                        )
                    ),
                    LiteralCollectionType.Tuple, Location.NoProvided,
                ),
            )
    }
    "custom operator precedence test" {
        "10 +> 2 * 3"
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "Operator10",
                    "+>",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    OperatorNode(
                        "Operator11",
                        "*",
                        LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                        LiteralValueNode("3", LiteralValueType.Integer, Location.NoProvided),
                        Location.NoProvided,
                    ),
                    Location.NoProvided,
                )
            )
    }
    "collection with expressions" {
        "[10 , 2 + 1]"
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                LiteralCollectionNode(
                    listOf(
                        LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                        OperatorNode(
                            "Operator10",
                            "+",
                            LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                            LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided,
                        )
                    ),
                    LiteralCollectionType.List, Location.NoProvided,
                ),
            )
    }
    "map with expressions" {
        "{(10 + 2): 10 + 20, \"key2\": 20}"
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                LiteralCollectionNode(
                    listOf(
                        LiteralMapEntryNode(
                            OperatorNode(
                                "Operator10",
                                "+",
                                LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                                LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                                Location.NoProvided,
                            ),
                            OperatorNode(
                                "Operator10",
                                "+",
                                LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                                LiteralValueNode("20", LiteralValueType.Integer, Location.NoProvided),
                                Location.NoProvided,
                            ), Location.NoProvided, LiteralMapEntryNodeLocations(Location.NoProvided)
                        ),
                        LiteralMapEntryNode(
                            LiteralValueNode("\"key2\"", LiteralValueType.String, Location.NoProvided),
                            LiteralValueNode("20", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided, LiteralMapEntryNodeLocations(Location.NoProvided)
                        )
                    ),
                    LiteralCollectionType.Map,
                    Location.NoProvided,
                ),
            )
    }
    "block expressions" {
        """|sum 10
           |    20
           |    30 + 15
        """.trimMargin().also(::println)
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
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
                            "Operator10",
                            "+",
                            LiteralValueNode("30", LiteralValueType.Integer, Location.NoProvided),
                            LiteralValueNode("15", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided,
                        ),
                    ),
                    Location.NoProvided
                )
            )
    }
    "function and operators in block expressions" {
        """
        |10 +
        |    sum 5
        |        30 + 15
        """.trimMargin()
            .also { println(it) }
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "Operator10",
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
                                "Operator10",
                                "+",
                                LiteralValueNode("30", LiteralValueType.Integer, Location.NoProvided),
                                LiteralValueNode("15", LiteralValueType.Integer, Location.NoProvided),
                                Location.NoProvided,
                            ),
                        ),
                        Location.NoProvided
                    ),
                    Location.NoProvided,
                )
            )
    }
    "function and operators in block expressions 2" {
        """10
           | >> sum 15
           | >> 20
        """.trimMargin()
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value.also(::println) }
            .shouldBeRight(
                OperatorNode(
                    "Operator9",
                    operator = ">>",
                    OperatorNode(
                        "Operator9",
                        ">>",
                        LiteralValueNode(value = "10", type = LiteralValueType.Integer, Location.NoProvided),
                        FunctionCallNode(
                            "sum",
                            FunctionType.Function,
                            listOf(
                                LiteralValueNode(
                                    "15",
                                    LiteralValueType.Integer,
                                    Location.NoProvided
                                )
                            ),
                            Location.NoProvided
                        ),
                        Location.NoProvided
                    ),
                    LiteralValueNode(
                        value = "20",
                        type = LiteralValueType.Integer,
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
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                OperatorNode(
                    "Operator10",
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
                    Location.NoProvided,
                ),
            )
    }
    "complete if expression" {
        "if 4 > a then 10 else 20"
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                IfNode(
                    OperatorNode(
                        "Operator8",
                        ">",
                        LiteralValueNode("4", LiteralValueType.Integer, Location.NoProvided),
                        FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                        Location.NoProvided,
                    ),
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("20", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided, IfNodeLocations(Location.NoProvided, Location.NoProvided, Location.NoProvided)
                )
            )
    }
    "complete if without else expression" {
        "if 4 > a then 10"
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                IfNode(
                    OperatorNode(
                        "Operator8",
                        ">",
                        LiteralValueNode("4", LiteralValueType.Integer, Location.NoProvided),
                        FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                        Location.NoProvided,
                    ),
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    UnitNode(Location.NoProvided),
                    Location.NoProvided, IfNodeLocations(Location.NoProvided, Location.NoProvided, Location.NoProvided)
                )
            )
    }
    "complete if in block expression" {
        """if 4 > a 
           |    then 10
           |    else 20""".trimMargin()
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                IfNode(
                    OperatorNode(
                        "Operator8",
                        ">",
                        LiteralValueNode("4", LiteralValueType.Integer, Location.NoProvided),
                        FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                        Location.NoProvided,
                    ),
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("20", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided, IfNodeLocations(Location.NoProvided, Location.NoProvided, Location.NoProvided)
                )
            )
    }
    "function with if expressions" {
        """|sum 10
           |    if 1 != 2 
           |      then 1
           |      else 2
           |    15
        """.trimMargin()
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
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
                                "Operator7",
                                "!=",
                                LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                                LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                                Location.NoProvided,
                            ),
                            LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                            LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided,
                            IfNodeLocations(Location.NoProvided, Location.NoProvided, Location.NoProvided)
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
        """|sum 10
           |    if 1 != 2 
           |      then 1
           |    15
        """.trimMargin()
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
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
                                "Operator7",
                                "!=",
                                LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                                LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                                Location.NoProvided,
                            ),
                            LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                            UnitNode(Location.NoProvided),
                            Location.NoProvided,
                            IfNodeLocations(Location.NoProvided, Location.NoProvided, Location.NoProvided)
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
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                IfNode(
                    OperatorNode(
                        "Operator8",
                        ">",
                        LiteralValueNode("4", LiteralValueType.Integer, Location.NoProvided),
                        FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                        Location.NoProvided,
                    ),
                    UnitNode(Location.NoProvided),
                    UnitNode(Location.NoProvided),
                    Location.NoProvided, IfNodeLocations(Location.NoProvided, Location.NoProvided, Location.NoProvided)
                )
            )
    }
    "Type instance with labels" {
        "Username label1: x label2: 20"
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
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
    "let expression" {
        """
           |let x = 10
           |    y = 20
           |then x + y
        """.trimMargin()
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                LetExpressionNode(
                    listOf(
                        MatchAssignNode(
                            FunctionCallNode("x", FunctionType.Function, listOf(), Location.NoProvided),
                            LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided
                        ),
                        MatchAssignNode(
                            FunctionCallNode("y", FunctionType.Function, listOf(), Location.NoProvided),
                            LiteralValueNode("20", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided
                        )
                    ),
                    OperatorNode(
                        "Operator10",
                        "+",
                        FunctionCallNode("x", FunctionType.Function, listOf(), Location.NoProvided),
                        FunctionCallNode("y", FunctionType.Function, listOf(), Location.NoProvided),
                        Location.NoProvided,
                    ),
                    Location.NoProvided,
                    LetExpressionNodeLocations(Location.NoProvided, Location.NoProvided)
                )
            )
    }
    "let expression 2" {
        """let x = sum 10       
           |   y = 20
           |then x + y
        """.trimMargin()
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                LetExpressionNode(
                    listOf(
                        MatchAssignNode(
                            FunctionCallNode("x", FunctionType.Function, listOf(), Location.NoProvided),
                            FunctionCallNode(
                                "sum", FunctionType.Function, listOf(
                                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided)
                                ), Location.NoProvided
                            ),
                            Location.NoProvided,
                        ),
                        MatchAssignNode(
                            FunctionCallNode("y", FunctionType.Function, listOf(), Location.NoProvided),
                            LiteralValueNode("20", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided,
                        )
                    ),
                    OperatorNode(
                        "Operator10",
                        "+",
                        FunctionCallNode("x", FunctionType.Function, listOf(), Location.NoProvided),
                        FunctionCallNode("y", FunctionType.Function, listOf(), Location.NoProvided),
                        Location.NoProvided,
                    ),
                    Location.NoProvided,
                    LetExpressionNodeLocations(Location.NoProvided, Location.NoProvided)
                )
            )
    }
    "let expression 3" {
        "let [ x | y ] = [1, 2] then x + y".trimMargin()
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                LetExpressionNode(
                    matches = listOf(
                        MatchAssignNode(
                            MatchListValueNode(
                                head = listOf(
                                    FunctionCallNode(
                                        name = "x", type = FunctionType.Function, arguments = listOf(),
                                        location = Location.NoProvided
                                    )
                                ),
                                tail = LiteralValueNode(
                                    value = "y", type = LiteralValueType.Binding,
                                    location = Location.NoProvided
                                ),
                                location = Location.NoProvided,
                                locations = MatchListValueNodeLocations(tailSeparatorLocation = Location.NoProvided)
                            ),
                            expression = LiteralCollectionNode(
                                values = listOf(
                                    LiteralValueNode(
                                        value = "1",
                                        type = LiteralValueType.Integer,
                                        location = Location.NoProvided
                                    ),
                                    LiteralValueNode(
                                        value = "2", type = LiteralValueType.Integer,
                                        location = Location.NoProvided
                                    )
                                ), type = LiteralCollectionType.List, location = Location.NoProvided
                            ),
                            location = Location.NoProvided,
                        )
                    ),
                    expression = OperatorNode(
                        "Operator10",
                        operator = "+",
                        left = FunctionCallNode(
                            name = "x",
                            type = FunctionType.Function,
                            arguments = listOf(),
                            location = Location.NoProvided
                        ),
                        right = FunctionCallNode(
                            name = "y",
                            type = FunctionType.Function,
                            arguments = listOf(),
                            location = Location.NoProvided
                        ),
                        location = Location.NoProvided
                    ),
                    location = Location.NoProvided,
                    locations = LetExpressionNodeLocations(
                        letLocation = Location.NoProvided,
                        thenLocation = Location.NoProvided
                    )
                )
            )
    }
    "let expression 4" {
        """let x && isEven x = 10       
           |then x
        """.trimMargin()
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value.also(::println) }
            .shouldBeRight(
                LetExpressionNode(
                    matches = listOf(
                        MatchAssignNode(
                            match = MatchConditionValueNode(
                                type = MatchConditionalType.And,
                                left = FunctionCallNode(
                                    name = "x",
                                    type = FunctionType.Function,
                                    arguments = listOf(),
                                    Location.NoProvided
                                ),
                                right = FunctionCallNode(
                                    name = "isEven",
                                    type = FunctionType.Function,
                                    arguments = listOf(
                                        LiteralValueNode(
                                            value = "x",
                                            type = LiteralValueType.Binding,
                                            Location.NoProvided
                                        )
                                    ),
                                    Location.NoProvided
                                ),
                                Location.NoProvided
                            ),
                            expression = LiteralValueNode(
                                value = "10",
                                type = LiteralValueType.Integer,
                                Location.NoProvided
                            ),
                            Location.NoProvided,
                        )
                    ),
                    expression = FunctionCallNode(
                        name = "x",
                        type = FunctionType.Function,
                        arguments = listOf(),
                        Location.NoProvided
                    ), Location.NoProvided,
                    locations = LetExpressionNodeLocations(
                        letLocation = Location.NoProvided,
                        thenLocation = Location.NoProvided
                    )
                )
            )
    }
    "nested let expression" {
        """|let x = let a2 = a * 2
           |            b2 = b * 2
           |        then a2 + b2
           |then x + 2
        """.trimMargin().also(::println)
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                LetExpressionNode(
                    listOf(
                        MatchAssignNode(
                            FunctionCallNode("x", FunctionType.Function, listOf(), Location.NoProvided),
                            LetExpressionNode(
                                listOf(
                                    MatchAssignNode(
                                        FunctionCallNode(
                                            "a2",
                                            FunctionType.Function,
                                            listOf(),
                                            Location.NoProvided
                                        ),
                                        OperatorNode(
                                            "Operator11",
                                            "*",
                                            FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                                            LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                                            Location.NoProvided,
                                        ),
                                        Location.NoProvided,
                                    ),
                                    MatchAssignNode(
                                        FunctionCallNode(
                                            "b2",
                                            FunctionType.Function,
                                            listOf(),
                                            Location.NoProvided
                                        ),
                                        OperatorNode(
                                            "Operator11",
                                            "*",
                                            FunctionCallNode("b", FunctionType.Function, listOf(), Location.NoProvided),
                                            LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                                            Location.NoProvided,
                                        ),
                                        Location.NoProvided
                                    )
                                ),
                                OperatorNode(
                                    "Operator10",
                                    "+",
                                    FunctionCallNode("a2", FunctionType.Function, listOf(), Location.NoProvided),
                                    FunctionCallNode("b2", FunctionType.Function, listOf(), Location.NoProvided),
                                    Location.NoProvided,
                                ),
                                Location.NoProvided,
                                LetExpressionNodeLocations(Location.NoProvided, Location.NoProvided)
                            ),
                            Location.NoProvided,
                        )
                    ),
                    OperatorNode(
                        "Operator10",
                        "+",
                        FunctionCallNode("x", FunctionType.Function, listOf(), Location.NoProvided),
                        LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                        Location.NoProvided,
                    ),
                    Location.NoProvided, LetExpressionNodeLocations(Location.NoProvided, Location.NoProvided)
                )
            )
    }
    "match expression" {
        """match 1 with
           |   1 then "one"
           |   2 || 3 then "other"
        """.trimMargin()
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                MatchExpressionNode(
                    LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                    listOf(
                        MatchExpressionBranchNode(
                            LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                            LiteralValueNode("\"one\"", LiteralValueType.String, Location.NoProvided),
                            Location.NoProvided
                        ),
                        MatchExpressionBranchNode(
                            MatchConditionValueNode(
                                type = MatchConditionalType.Or,
                                left = LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                                right = LiteralValueNode("3", LiteralValueType.Integer, Location.NoProvided),
                                Location.NoProvided
                            ),
                            LiteralValueNode("\"other\"", LiteralValueType.String, Location.NoProvided),
                            Location.NoProvided
                        )
                    ),
                    Location.NoProvided,
                    MatchExpressionNodeLocations(Location.NoProvided, Location.NoProvided)
                )
            )
    }
    "match expression ambiguity" {
        """|match 1 with
           |      [x, y] then x + y
           |      z then True
        """.trimMargin()
            .kSharpLexer()
            .prepareLexerForExpressionParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                MatchExpressionNode(
                    expression = LiteralValueNode(value = "1", type = LiteralValueType.Integer, Location.NoProvided),
                    branches = listOf(
                        MatchExpressionBranchNode(
                            match = LiteralCollectionNode(
                                values = listOf(
                                    FunctionCallNode(
                                        name = "x",
                                        type = FunctionType.Function,
                                        arguments = listOf(),
                                        Location.NoProvided
                                    ),
                                    FunctionCallNode(
                                        name = "y",
                                        type = FunctionType.Function,
                                        arguments = listOf(),
                                        Location.NoProvided
                                    )
                                ),
                                type = LiteralCollectionType.List, Location.NoProvided
                            ),
                            expression = OperatorNode(
                                category = "Operator10",
                                operator = "+",
                                left = FunctionCallNode(
                                    name = "x",
                                    type = FunctionType.Function,
                                    arguments = listOf(),
                                    Location.NoProvided
                                ),
                                right = FunctionCallNode(
                                    name = "y",
                                    type = FunctionType.Function,
                                    arguments = listOf(),
                                    Location.NoProvided
                                ),
                                Location.NoProvided
                            ),
                            Location.NoProvided
                        ),
                        MatchExpressionBranchNode(
                            match = FunctionCallNode(
                                name = "z",
                                type = FunctionType.Function,
                                arguments = listOf(),
                                Location.NoProvided
                            ),
                            expression = FunctionCallNode(
                                name = "True",
                                type = FunctionType.TypeInstance,
                                arguments = listOf(),
                                Location.NoProvided
                            ), Location.NoProvided
                        )
                    ),
                    Location.NoProvided,
                    locations = MatchExpressionNodeLocations(
                        matchLocation = Location.NoProvided,
                        withLocation = Location.NoProvided
                    )
                )
            )
    }
})
