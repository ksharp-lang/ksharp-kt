package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Location
import org.ksharp.nodes.*
import org.ksharp.test.shouldBeRight

class LiteralParserTest : StringSpec({
    "Character" {
        "'a'"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeLiteral()
            .map { it.value }
            .shouldBeRight(LiteralValueNode("'a'", LiteralValueType.Character, Location.NoProvided))
    }
    "String" {
        "\"a\""
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeLiteral()
            .map { it.value }
            .shouldBeRight(LiteralValueNode("\"a\"", LiteralValueType.String, Location.NoProvided))
    }
    "Multiline String" {
        "\"\"\"Hello\"\"\""
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeLiteral()
            .map { it.value }
            .shouldBeRight(LiteralValueNode("\"\"\"Hello\"\"\"", LiteralValueType.MultiLineString, Location.NoProvided))
    }
    "Integer" {
        "1_000"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeLiteral()
            .map { it.value }
            .shouldBeRight(LiteralValueNode("1_000", LiteralValueType.Integer, Location.NoProvided))
    }
    "Hex number" {
        "0xFFFF"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeLiteral()
            .map { it.value }
            .shouldBeRight(LiteralValueNode("0xFFFF", LiteralValueType.HexInteger, Location.NoProvided))
    }
    "Octal number" {
        "0o1234"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeLiteral()
            .map { it.value }
            .shouldBeRight(LiteralValueNode("0o1234", LiteralValueType.OctalInteger, Location.NoProvided))
    }
    "Binary number" {
        "0b0010"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeLiteral()
            .map { it.value }
            .shouldBeRight(LiteralValueNode("0b0010", LiteralValueType.BinaryInteger, Location.NoProvided))
    }
    "Decimal" {
        "1.6"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeLiteral()
            .map { it.value }
            .shouldBeRight(LiteralValueNode("1.6", LiteralValueType.Decimal, Location.NoProvided))
    }
    "List" {
        "[1, 2, 3]"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeLiteral()
            .map { it.value }
            .shouldBeRight(
                LiteralCollectionNode(
                    listOf(
                        LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                        LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                        LiteralValueNode("3", LiteralValueType.Integer, Location.NoProvided)
                    ),
                    LiteralCollectionType.List,
                    Location.NoProvided
                )
            )
    }
    "Set" {
        "#[1, 2, 3]"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeLiteral()
            .map { it.value }
            .shouldBeRight(
                LiteralCollectionNode(
                    listOf(
                        LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                        LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                        LiteralValueNode("3", LiteralValueType.Integer, Location.NoProvided)
                    ),
                    LiteralCollectionType.Set,
                    Location.NoProvided
                )
            )
    }
    "Map" {
        "{\"key1\": 1, \"key2\": 2}"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeLiteral()
            .map { it.value }
            .shouldBeRight(
                LiteralCollectionNode(
                    listOf(
                        LiteralMapEntryNode(
                            LiteralValueNode("\"key1\"", LiteralValueType.String, Location.NoProvided),
                            LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided
                        ),
                        LiteralMapEntryNode(
                            LiteralValueNode("\"key2\"", LiteralValueType.String, Location.NoProvided),
                            LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided
                        )
                    ), LiteralCollectionType.Map, Location.NoProvided
                )
            )
    }
    "Tuple" {
        "1, 2, 3"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                LiteralCollectionNode(
                    listOf(
                        LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                        LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                        LiteralValueNode("3", LiteralValueType.Integer, Location.NoProvided)
                    ), LiteralCollectionType.Tuple, Location.NoProvided
                )
            )

        "(1, 2, 3)"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                LiteralCollectionNode(
                    listOf(
                        LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                        LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                        LiteralValueNode("3", LiteralValueType.Integer, Location.NoProvided)
                    ), LiteralCollectionType.Tuple, Location.NoProvided
                )
            )
    }
    "List of tuples" {
        "[(1, 2), (2, 3)]"
            .kSharpLexer()
            .collapseKSharpTokens()
            .consumeExpression()
            .map { it.value }
            .shouldBeRight(
                LiteralCollectionNode(
                    listOf(
                        LiteralCollectionNode(
                            listOf(
                                LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                                LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided)
                            ), LiteralCollectionType.Tuple, Location.NoProvided
                        ),
                        LiteralCollectionNode(
                            listOf(
                                LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                                LiteralValueNode("3", LiteralValueType.Integer, Location.NoProvided)
                            ), LiteralCollectionType.Tuple, Location.NoProvided
                        )
                    ), LiteralCollectionType.List, Location.NoProvided
                )
            )
    }
})