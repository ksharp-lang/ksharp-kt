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
            .consumeExpressionValue()
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
    "function call receiving tuples" {
        "moveX 10,20 5"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpressionValue()
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
            .consumeExpressionValue()
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
            .consumeExpressionValue()
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
            .consumeExpressionValue()
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
})