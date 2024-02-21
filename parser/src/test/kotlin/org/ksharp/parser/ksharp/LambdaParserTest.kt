package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Location
import org.ksharp.nodes.*
import org.ksharp.parser.TokenLexerIterator
import org.ksharp.parser.collapseNewLines
import org.ksharp.parser.enableLookAhead
import org.ksharp.parser.excludeIgnoreNewLineTokens
import org.ksharp.test.shouldBeRight

private fun TokenLexerIterator<KSharpLexerState>.prepareLexerForLambdaParsing() =
    filterAndCollapseTokens()
        .excludeIgnoreNewLineTokens()
        .collapseNewLines()
        .enableLookAhead()
        .enableIndentationOffset()

class LambdaParserTest : StringSpec({
    "Parse lambda with one arguments" {
        """
            \x -> x + 10
        """.trimIndent()
            .kSharpLexer()
            .prepareLexerForLambdaParsing()
            .consumeLambdaExpression()
            .map { it.value }
            .shouldBeRight(
                LambdaNode(
                    parameters = listOf("x"),
                    expression = OperatorNode(
                        category = "Operator10", operator = "+",
                        left = FunctionCallNode(
                            name = "x", type = FunctionType.Function, arguments = emptyList(),
                            location = Location.NoProvided
                        ),
                        right = LiteralValueNode(
                            value = "10", type = LiteralValueType.Integer,
                            location = Location.NoProvided
                        ), location = Location.NoProvided
                    ),
                    location = Location.NoProvided,
                    locations = LambdaNodeLocations(
                        assignOperator = Location.NoProvided,
                        parameters = listOf(Location.NoProvided)
                    )
                )
            )
    }
    "Parse lambda with many arguments" {
        """
            \x y 
               -> x + y + 10
        """.trimIndent()
            .kSharpLexer()
            .prepareLexerForLambdaParsing()
            .consumeLambdaExpression()
            .map { it.value.also { println(it) } }
            .shouldBeRight(
                LambdaNode(
                    parameters = listOf("x", "y"),
                    expression = OperatorNode(
                        category = "Operator10",
                        operator = "+",
                        left = OperatorNode(
                            category = "Operator10", operator = "+",
                            left = FunctionCallNode(
                                name = "x", type = FunctionType.Function, arguments = emptyList(),
                                location = Location.NoProvided
                            ),
                            right = FunctionCallNode(
                                name = "y", type = FunctionType.Function, arguments = emptyList(),
                                location = Location.NoProvided
                            ),
                            location = Location.NoProvided
                        ),
                        right = LiteralValueNode(
                            value = "10", type = LiteralValueType.Integer,
                            location = Location.NoProvided
                        ),
                        location = Location.NoProvided
                    ),
                    location = Location.NoProvided,
                    locations = LambdaNodeLocations(
                        assignOperator = Location.NoProvided,
                        parameters = (listOf(
                            Location.NoProvided,
                            Location.NoProvided
                        ))
                    )
                )
            )
    }
    "Parse lambda with many arguments 2" {
        """
            \x y -> 
                 x + y + 10
        """.trimIndent()
            .kSharpLexer()
            .prepareLexerForLambdaParsing()
            .consumeLambdaExpression()
            .map { it.value.also { println(it) } }
            .shouldBeRight(
                LambdaNode(
                    parameters = listOf("x", "y"),
                    expression = OperatorNode(
                        category = "Operator10",
                        operator = "+",
                        left = OperatorNode(
                            category = "Operator10", operator = "+",
                            left = FunctionCallNode(
                                name = "x", type = FunctionType.Function, arguments = emptyList(),
                                location = Location.NoProvided
                            ),
                            right = FunctionCallNode(
                                name = "y", type = FunctionType.Function, arguments = emptyList(),
                                location = Location.NoProvided
                            ),
                            location = Location.NoProvided
                        ),
                        right = LiteralValueNode(
                            value = "10", type = LiteralValueType.Integer,
                            location = Location.NoProvided
                        ),
                        location = Location.NoProvided
                    ),
                    location = Location.NoProvided,
                    locations = LambdaNodeLocations(
                        assignOperator = Location.NoProvided,
                        parameters = (listOf(
                            Location.NoProvided,
                            Location.NoProvided
                        ))
                    )
                )
            )
    }
    "Parse unit lambda" {
        """
            \-> 10
        """.trimIndent()
            .kSharpLexer()
            .prepareLexerForLambdaParsing()
            .consumeUnitLambdaExpression()
            .map { it.value }
            .shouldBeRight(
                LambdaNode(
                    parameters = emptyList(),
                    expression = LiteralValueNode(
                        value = "10", type = LiteralValueType.Integer,
                        location = Location.NoProvided
                    ),
                    location = Location.NoProvided,
                    locations = LambdaNodeLocations(
                        assignOperator = Location.NoProvided,
                        parameters = emptyList()
                    )
                )
            )
    }
    "Parse unit lambda 2" {
        """
            \-> 
                10
        """.trimIndent()
            .kSharpLexer()
            .prepareLexerForLambdaParsing()
            .consumeUnitLambdaExpression()
            .map { it.value }
            .shouldBeRight(
                LambdaNode(
                    parameters = emptyList(),
                    expression = LiteralValueNode(
                        value = "10", type = LiteralValueType.Integer,
                        location = Location.NoProvided
                    ),
                    location = Location.NoProvided,
                    locations = LambdaNodeLocations(
                        assignOperator = Location.NoProvided,
                        parameters = emptyList()
                    )
                )
            )
    }
})
