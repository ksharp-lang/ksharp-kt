package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Location
import org.ksharp.nodes.*
import org.ksharp.parser.TokenLexerIterator
import org.ksharp.parser.enableLookAhead
import org.ksharp.test.shouldBeRight

private fun TokenLexerIterator<KSharpLexerState>.prepareLexerForLiteralParsing() =
    filterAndCollapseTokens()
        .enableLookAhead()


class LiteralParserTest : StringSpec({
    "Character" {
        "'a'"
            .kSharpLexer()
            .prepareLexerForLiteralParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(LiteralValueNode("'a'", LiteralValueType.Character, Location.NoProvided))
    }
    "String" {
        "\"a\""
            .kSharpLexer()
            .prepareLexerForLiteralParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(LiteralValueNode("\"a\"", LiteralValueType.String, Location.NoProvided))
    }
    "Multiline String" {
        "\"\"\"Hello\"\"\""
            .kSharpLexer()
            .prepareLexerForLiteralParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(LiteralValueNode("\"\"\"Hello\"\"\"", LiteralValueType.MultiLineString, Location.NoProvided))
    }
    "Integer" {
        "1_000"
            .kSharpLexer()
            .prepareLexerForLiteralParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(LiteralValueNode("1_000", LiteralValueType.Integer, Location.NoProvided))
    }
    "Hex number" {
        "0xFFFF"
            .kSharpLexer()
            .prepareLexerForLiteralParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(LiteralValueNode("0xFFFF", LiteralValueType.HexInteger, Location.NoProvided))
    }
    "Octal number" {
        "0o1234"
            .kSharpLexer()
            .prepareLexerForLiteralParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(LiteralValueNode("0o1234", LiteralValueType.OctalInteger, Location.NoProvided))
    }
    "Binary number" {
        "0b0010"
            .kSharpLexer()
            .prepareLexerForLiteralParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(LiteralValueNode("0b0010", LiteralValueType.BinaryInteger, Location.NoProvided))
    }
    "Decimal" {
        "1.6"
            .kSharpLexer()
            .prepareLexerForLiteralParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(LiteralValueNode("1.6", LiteralValueType.Decimal, Location.NoProvided))
    }
    "List" {
        "[1, 2, 3]"
            .kSharpLexer()
            .prepareLexerForLiteralParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                LiteralCollectionNode(
                    listOf(
                        LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                        LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                        LiteralValueNode("3", LiteralValueType.Integer, Location.NoProvided)
                    ),
                    LiteralCollectionType.List,
                    Location.NoProvided,
                )
            )
    }
    "Set" {
        "#[1, 2, 3]"
            .kSharpLexer()
            .prepareLexerForLiteralParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                LiteralCollectionNode(
                    listOf(
                        LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                        LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                        LiteralValueNode("3", LiteralValueType.Integer, Location.NoProvided)
                    ),
                    LiteralCollectionType.Set,
                    Location.NoProvided,
                )
            )
    }
    "Map" {
        "{\"key1\": 1, \"key2\": 2}"
            .kSharpLexer()
            .prepareLexerForLiteralParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                LiteralCollectionNode(
                    listOf(
                        LiteralMapEntryNode(
                            LiteralValueNode("\"key1\"", LiteralValueType.String, Location.NoProvided),
                            LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided, LiteralMapEntryNodeLocations(Location.NoProvided)
                        ),
                        LiteralMapEntryNode(
                            LiteralValueNode("\"key2\"", LiteralValueType.String, Location.NoProvided),
                            LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided, LiteralMapEntryNodeLocations(Location.NoProvided)
                        )
                    ),
                    LiteralCollectionType.Map,
                    Location.NoProvided,
                )
            )
    }
    "Tuple" {
        "1, 2, 3"
            .kSharpLexer()
            .prepareLexerForLiteralParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                LiteralCollectionNode(
                    listOf(
                        LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                        LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                        LiteralValueNode("3", LiteralValueType.Integer, Location.NoProvided)
                    ),
                    LiteralCollectionType.Tuple,
                    Location.NoProvided,
                )
            )

        "(1, 2, 3)"
            .kSharpLexer()
            .prepareLexerForLiteralParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                LiteralCollectionNode(
                    listOf(
                        LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                        LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                        LiteralValueNode("3", LiteralValueType.Integer, Location.NoProvided)
                    ),
                    LiteralCollectionType.Tuple,
                    Location.NoProvided,
                )
            )
    }
    "Tuples with function calls" {
        "x, y"
            .kSharpLexer()
            .prepareLexerForLiteralParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                LiteralCollectionNode(
                    listOf(
                        FunctionCallNode("x", FunctionType.Function, emptyList(), Location.NoProvided),
                        FunctionCallNode("y", FunctionType.Function, emptyList(), Location.NoProvided),
                    ),
                    LiteralCollectionType.Tuple,
                    Location.NoProvided,
                )
            )
    }
    "List of tuples" {
        "[(1, 2), (2, 3)]"
            .kSharpLexer()
            .prepareLexerForLiteralParsing()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                LiteralCollectionNode(
                    listOf(
                        LiteralCollectionNode(
                            listOf(
                                LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                                LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided)
                            ),
                            LiteralCollectionType.Tuple,
                            Location.NoProvided,
                        ),
                        LiteralCollectionNode(
                            listOf(
                                LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                                LiteralValueNode("3", LiteralValueType.Integer, Location.NoProvided)
                            ),
                            LiteralCollectionType.Tuple,
                            Location.NoProvided
                        )
                    ),
                    LiteralCollectionType.List,
                    Location.NoProvided,
                )
            )
    }
    "Binding" {
        "map"
            .kSharpLexer()
            .prepareLexerForLiteralParsing()
            .consumeExpressionValue(withBindings = true)
            .map { it.value }
            .shouldBeRight(
                LiteralValueNode("map", LiteralValueType.Binding, Location.NoProvided),
            )
    }
    "Type Instance Binding" {
        "Point"
            .kSharpLexer()
            .prepareLexerForLiteralParsing()
            .consumeExpressionValue(withBindings = true)
            .map { it.value }
            .shouldBeRight(
                LiteralValueNode("Point", LiteralValueType.Binding, Location.NoProvided),
            )
    }
    "Function name Binding" {
        "point2d->point3d"
            .kSharpLexer()
            .prepareLexerForLiteralParsing()
            .consumeExpressionValue(withBindings = true)
            .map { it.value }
            .shouldBeRight(
                LiteralValueNode("point2d->point3d", LiteralValueType.Binding, Location.NoProvided),
            )
    }
    "Operator Binding" {
        "(+)"
            .kSharpLexer()
            .prepareLexerForLiteralParsing()
            .consumeExpressionValue(withBindings = true)
            .map { it.value }
            .shouldBeRight(
                LiteralValueNode("(+)", LiteralValueType.OperatorBinding, Location.NoProvided),
            )
    }
    "Unit literal" {
        "()"
            .kSharpLexer()
            .prepareLexerForLiteralParsing()
            .consumeExpressionValue(withBindings = true)
            .map { it.value }
            .shouldBeRight(
                UnitNode(Location.NoProvided),
            )
    }
})
