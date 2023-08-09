package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Location
import org.ksharp.nodes.*
import org.ksharp.parser.TokenLexerIterator
import org.ksharp.parser.enableLookAhead
import org.ksharp.test.shouldBeRight

private fun TokenLexerIterator<KSharpLexerState>.prepareLexerForTypeParsing() =
    filterAndCollapseTokens()
        .collapseNewLines()
        .enableLookAhead()
        .enableIndentationOffset()

class TraitParsingTest : StringSpec({
    "Parsing a trait" {
        """
            trait Num a =
                sum :: a -> a -> a
                prod :: a -> a -> a
        """.trimIndent()
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TraitNode(
                    false,
                    null,
                    "Num",
                    listOf("a"),
                    TraitFunctionsNode(
                        listOf(
                            TraitFunctionNode(
                                "sum",
                                FunctionTypeNode(
                                    listOf(
                                        ParameterTypeNode("a", Location.NoProvided),
                                        ParameterTypeNode("a", Location.NoProvided),
                                        ParameterTypeNode("a", Location.NoProvided)
                                    ),
                                    Location.NoProvided, FunctionTypeNodeLocations(listOf())
                                ),
                                Location.NoProvided, TraitFunctionNodeLocation(Location.NoProvided, Location.NoProvided)
                            ),
                            TraitFunctionNode(
                                "prod",
                                FunctionTypeNode(
                                    listOf(
                                        ParameterTypeNode("a", Location.NoProvided),
                                        ParameterTypeNode("a", Location.NoProvided),
                                        ParameterTypeNode("a", Location.NoProvided)
                                    ),
                                    Location.NoProvided, FunctionTypeNodeLocations(listOf())
                                ),
                                Location.NoProvided, TraitFunctionNodeLocation(Location.NoProvided, Location.NoProvided)
                            )
                        ), emptyList()
                    ),
                    Location.NoProvided,
                    TraitNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Parsing a trait with default impl on a method" {
        """
            trait Num a =
                sum :: a -> a -> a
                prod :: a -> a -> a
                
                sum a b = a + b
        """.trimIndent()
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value.also(::println) }
            .shouldBeRight()
    }
})
