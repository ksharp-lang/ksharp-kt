package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Location
import org.ksharp.nodes.*
import org.ksharp.parser.LexerToken
import org.ksharp.parser.TextToken
import org.ksharp.test.shouldBeRight

class FunctionParserTest : StringSpec({
    "native function" {
        "native sum a b"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeFunction)
            .map { it.value }
            .shouldBeRight(
                FunctionNode(
                    true,
                    false,
                    null,
                    "sum",
                    listOf("a", "b"),
                    UnitNode(Location.NoProvided),
                    Location.NoProvided,
                    FunctionNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "native pub function" {
        "native pub sum a b"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeFunction)
            .map { it.value }
            .shouldBeRight(
                FunctionNode(
                    true,
                    true,
                    null,
                    "sum",
                    listOf("a", "b"),
                    UnitNode(Location.NoProvided),
                    Location.NoProvided,
                    FunctionNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "public function" {
        "pub sum a b = a + b"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeFunction)
            .map { it.value }
            .shouldBeRight(
                FunctionNode(
                    false,
                    true,
                    null,
                    "sum",
                    listOf("a", "b"),
                    OperatorNode(
                        "+",
                        FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                        FunctionCallNode("b", FunctionType.Function, listOf(), Location.NoProvided),
                        Location.NoProvided
                    ),
                    Location.NoProvided,
                    FunctionNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "if function" {
        "pub if a b = a + b"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeFunction)
            .map { it.value }
            .shouldBeRight(
                FunctionNode(
                    false,
                    true,
                    null,
                    "if",
                    listOf("a", "b"),
                    OperatorNode(
                        "+",
                        FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                        FunctionCallNode("b", FunctionType.Function, listOf(), Location.NoProvided),
                        Location.NoProvided,
                    ),
                    Location.NoProvided, FunctionNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "private function" {
        "sum a b = a + b"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeFunction)
            .map { it.value }
            .shouldBeRight(
                FunctionNode(
                    false,
                    false,
                    null,
                    "sum",
                    listOf("a", "b"),
                    OperatorNode(
                        "+",
                        FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                        FunctionCallNode("b", FunctionType.Function, listOf(), Location.NoProvided),
                        Location.NoProvided,
                    ),
                    Location.NoProvided, FunctionNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "function and block expression" {
        """sum a b = let a2 = a * 2
          |              b2 = b * 2
          |          then a2 + b2
        """.trimMargin()
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeFunction)
            .map { it.value }
            .shouldBeRight(
                FunctionNode(
                    false,
                    false,
                    null,
                    "sum",
                    listOf("a", "b"),
                    LetExpressionNode(
                        listOf(
                            MatchAssignNode(
                                MatchValueNode(
                                    MatchValueType.Expression,
                                    FunctionCallNode("a2", FunctionType.Function, listOf(), Location.NoProvided),
                                    Location.NoProvided
                                ),
                                OperatorNode(
                                    "*",
                                    FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                                    Location.NoProvided,
                                ),
                                Location.NoProvided,
                                MatchAssignNodeLocations(Location.NoProvided)
                            ),
                            MatchAssignNode(
                                MatchValueNode(
                                    MatchValueType.Expression,
                                    FunctionCallNode("b2", FunctionType.Function, listOf(), Location.NoProvided),
                                    Location.NoProvided
                                ),
                                OperatorNode(
                                    "*",
                                    FunctionCallNode("b", FunctionType.Function, listOf(), Location.NoProvided),
                                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                                    Location.NoProvided,
                                ),
                                Location.NoProvided,
                                MatchAssignNodeLocations(Location.NoProvided)
                            )
                        ),
                        OperatorNode(
                            "+",
                            FunctionCallNode("a2", FunctionType.Function, listOf(), Location.NoProvided),
                            FunctionCallNode("b2", FunctionType.Function, listOf(), Location.NoProvided),
                            Location.NoProvided,
                        ),
                        Location.NoProvided
                    ),
                    Location.NoProvided,
                    FunctionNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "operator function" {
        "(+) a b = a + b"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeFunction)
            .map { it.value }
            .shouldBeRight(
                FunctionNode(
                    false,
                    false,
                    null,
                    "(+)",
                    listOf("a", "b"),
                    OperatorNode(
                        "+",
                        FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                        FunctionCallNode("b", FunctionType.Function, listOf(), Location.NoProvided),
                        Location.NoProvided,
                    ),
                    Location.NoProvided,
                    FunctionNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "complex function names" {
        "internal->wire a b = a + b"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeFunction)
            .map { it.value }
            .shouldBeRight(
                FunctionNode(
                    false,
                    false,
                    null,
                    "internal->wire",
                    listOf("a", "b"),
                    OperatorNode(
                        "+",
                        FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                        FunctionCallNode("b", FunctionType.Function, listOf(), Location.NoProvided),
                        Location.NoProvided,
                    ),
                    Location.NoProvided,
                    FunctionNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
})
