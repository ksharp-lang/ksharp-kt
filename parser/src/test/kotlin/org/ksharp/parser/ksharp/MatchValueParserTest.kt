package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Location
import org.ksharp.nodes.*
import org.ksharp.parser.TokenLexerIterator
import org.ksharp.parser.enableLookAhead
import org.ksharp.test.shouldBeRight

private fun TokenLexerIterator<KSharpLexerState>.prepareLexerForMatchValueParsing() =
    filterAndCollapseTokens()
        .enableLookAhead()


class MatchValueParserTest : StringSpec({
    "Match binding" {
        "x".kSharpLexer()
            .prepareLexerForMatchValueParsing()
            .consumeMatchValue()
            .map { it.value }
            .shouldBeRight(
                MatchValueNode(
                    MatchValueType.Expression,
                    FunctionCallNode("x", FunctionType.Function, emptyList(), Location.NoProvided),
                    Location.NoProvided
                )
            )
    }
    "Match value" {
        "10".kSharpLexer()
            .prepareLexerForMatchValueParsing()
            .consumeMatchValue()
            .map { it.value }
            .shouldBeRight(
                MatchValueNode(
                    MatchValueType.Expression,
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided
                )
            )
    }
    "Match tuple" {
        "x, y".kSharpLexer()
            .prepareLexerForMatchValueParsing()
            .consumeMatchValue()
            .map { it.value }
            .shouldBeRight(
                MatchValueNode(
                    MatchValueType.Expression,
                    LiteralCollectionNode(
                        listOf(
                            FunctionCallNode("x", FunctionType.Function, emptyList(), Location.NoProvided),
                            FunctionCallNode("y", FunctionType.Function, emptyList(), Location.NoProvided),
                        ),
                        LiteralCollectionType.Tuple,
                        Location.NoProvided,
                    ),
                    Location.NoProvided
                )
            )
    }
    "Match list" {
        "[x, y]".kSharpLexer()
            .prepareLexerForMatchValueParsing()
            .consumeMatchValue()
            .map { it.value }
            .shouldBeRight(
                MatchValueNode(
                    MatchValueType.Expression,
                    LiteralCollectionNode(
                        listOf(
                            FunctionCallNode("x", FunctionType.Function, emptyList(), Location.NoProvided),
                            FunctionCallNode("y", FunctionType.Function, emptyList(), Location.NoProvided),
                        ),
                        LiteralCollectionType.List,
                        Location.NoProvided,
                    ),
                    Location.NoProvided
                )
            )
    }
    "Match list with remaining" {
        "[1, 2 | rest]".kSharpLexer()
            .prepareLexerForMatchValueParsing()
            .consumeMatchValue()
            .map { it.value }
            .shouldBeRight(
                MatchValueNode(
                    MatchValueType.List,
                    MatchListValueNode(
                        listOf(
                            LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                            LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided)
                        ),
                        LiteralValueNode("rest", LiteralValueType.Binding, Location.NoProvided),
                        Location.NoProvided,
                        MatchListValueNodeLocations(Location.NoProvided)
                    ),
                    Location.NoProvided
                )
            )
    }
    "Match list with remaining 2" {
        "[(1, 2) | rest]".kSharpLexer()
            .prepareLexerForMatchValueParsing()
            .consumeMatchValue()
            .map { it.value }
            .shouldBeRight(
                MatchValueNode(
                    MatchValueType.List,
                    MatchListValueNode(
                        listOf(
                            LiteralCollectionNode(
                                listOf(
                                    LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                                ),
                                LiteralCollectionType.Tuple,
                                Location.NoProvided,
                            )
                        ),
                        LiteralValueNode("rest", LiteralValueType.Binding, Location.NoProvided),
                        Location.NoProvided,
                        MatchListValueNodeLocations(Location.NoProvided)
                    ),
                    Location.NoProvided
                )
            )
    }
    "Match sets" {
        "#[x, 1]".kSharpLexer()
            .prepareLexerForMatchValueParsing()
            .consumeMatchValue()
            .map { it.value }
            .shouldBeRight(
                MatchValueNode(
                    MatchValueType.Expression,
                    LiteralCollectionNode(
                        listOf(
                            FunctionCallNode("x", FunctionType.Function, emptyList(), Location.NoProvided),
                            LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                        ),
                        LiteralCollectionType.Set,
                        Location.NoProvided,
                    ),
                    Location.NoProvided,
                )
            )
    }
    "Match maps" {
        "{\"key1\": x, \"key2\": y}".kSharpLexer()
            .prepareLexerForMatchValueParsing()
            .consumeMatchValue()
            .map { it.value }
            .shouldBeRight(
                MatchValueNode(
                    MatchValueType.Expression,
                    LiteralCollectionNode(
                        listOf(
                            LiteralMapEntryNode(
                                LiteralValueNode("\"key1\"", LiteralValueType.String, Location.NoProvided),
                                FunctionCallNode("x", FunctionType.Function, emptyList(), Location.NoProvided),
                                Location.NoProvided, LiteralMapEntryNodeLocations(Location.NoProvided)
                            ),
                            LiteralMapEntryNode(
                                LiteralValueNode("\"key2\"", LiteralValueType.String, Location.NoProvided),
                                FunctionCallNode("y", FunctionType.Function, emptyList(), Location.NoProvided),
                                Location.NoProvided, LiteralMapEntryNodeLocations(Location.NoProvided)
                            )
                        ),
                        LiteralCollectionType.Map,
                        Location.NoProvided,
                    ),
                    Location.NoProvided
                )
            )
    }
    "Match types" {
        "Bool x".kSharpLexer()
            .prepareLexerForMatchValueParsing()
            .consumeMatchValue()
            .map { it.value }
            .shouldBeRight(
                MatchValueNode(
                    MatchValueType.Expression,
                    FunctionCallNode(
                        "Bool",
                        FunctionType.TypeInstance,
                        listOf(
                            LiteralValueNode("x", LiteralValueType.Binding, Location.NoProvided),
                        ), Location.NoProvided
                    ),
                    Location.NoProvided
                )
            )
    }
    "Match types with labels" {
        "Username password: p".kSharpLexer()
            .prepareLexerForMatchValueParsing()
            .consumeMatchValue()
            .map { it.value }
            .shouldBeRight(
                MatchValueNode(
                    MatchValueType.Expression,
                    FunctionCallNode(
                        "Username",
                        FunctionType.TypeInstance,
                        listOf(
                            LiteralValueNode("password:", LiteralValueType.Label, Location.NoProvided),
                            LiteralValueNode("p", LiteralValueType.Binding, Location.NoProvided),
                        ), Location.NoProvided
                    ),
                    Location.NoProvided
                )
            )
    }
    "Conditional matches" {
        "x && isEven x".kSharpLexer()
            .prepareLexerForMatchValueParsing()
            .consumeMatchValue()
            .map { it.value.also(::println) }
            .shouldBeRight(
                MatchValueNode(
                    type = MatchValueType.Group, value = GroupMatchValueNode(
                        matches = listOf(
                            MatchValueNode(
                                type = MatchValueType.Expression,
                                value = FunctionCallNode(
                                    name = "x",
                                    type = FunctionType.Function,
                                    arguments = listOf(),
                                    Location.NoProvided
                                ),
                                Location.NoProvided
                            ), MatchValueNode(
                                type = MatchValueType.And, value = MatchValueNode(
                                    type = MatchValueType.Expression, value = FunctionCallNode(
                                        name = "isEven", type = FunctionType.Function,
                                        arguments = listOf(
                                            LiteralValueNode(
                                                value = "x",
                                                type = LiteralValueType.Binding,
                                                Location.NoProvided
                                            )
                                        ),
                                        Location.NoProvided
                                    ), Location.NoProvided
                                ), Location.NoProvided
                            )
                        ),
                        Location.NoProvided
                    ), Location.NoProvided
                )
            )
    }
    "Group matches" {
        "(x && isEven x)".kSharpLexer()
            .prepareLexerForMatchValueParsing()
            .consumeMatchValue()
            .map { it.value.also(::println) }
            .shouldBeRight(
                MatchValueNode(
                    type = MatchValueType.Group, value = GroupMatchValueNode(
                        matches = listOf(
                            MatchValueNode(
                                type = MatchValueType.Expression,
                                value = FunctionCallNode(
                                    name = "x",
                                    type = FunctionType.Function,
                                    arguments = listOf(),
                                    Location.NoProvided
                                ),
                                Location.NoProvided
                            ), MatchValueNode(
                                type = MatchValueType.And, value = MatchValueNode(
                                    type = MatchValueType.Expression, value = FunctionCallNode(
                                        name = "isEven", type = FunctionType.Function,
                                        arguments = listOf(
                                            LiteralValueNode(
                                                value = "x",
                                                type = LiteralValueType.Binding,
                                                Location.NoProvided
                                            )
                                        ),
                                        Location.NoProvided
                                    ), Location.NoProvided
                                ), Location.NoProvided
                            )
                        ),
                        Location.NoProvided
                    ), Location.NoProvided
                )
            )
    }
    "Match assignment" {
        "x, y = 1, 2".kSharpLexer()
            .prepareLexerForMatchValueParsing()
            .consumeMatchAssignment()
            .map { it.value }
            .shouldBeRight(
                MatchAssignNode(
                    MatchValueNode(
                        MatchValueType.Expression,
                        LiteralCollectionNode(
                            listOf(
                                FunctionCallNode("x", FunctionType.Function, listOf(), Location.NoProvided),
                                FunctionCallNode("y", FunctionType.Function, listOf(), Location.NoProvided),
                            ),
                            LiteralCollectionType.Tuple,
                            Location.NoProvided,
                        ),
                        Location.NoProvided
                    ),
                    LiteralCollectionNode(
                        listOf(
                            LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                            LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided)
                        ),
                        LiteralCollectionType.Tuple,
                        Location.NoProvided,
                    ),
                    Location.NoProvided, MatchAssignNodeLocations(Location.NoProvided)
                )
            )
    }
})
