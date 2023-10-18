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
    "Parsing a trait with operator functions" {
        """
            trait Num a =
                (+) :: a -> a -> a
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
                                "(+)",
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
            .map { it.value }
            .shouldBeRight(
                TraitNode(
                    false,
                    null,
                    "Num",
                    params = listOf("a"),
                    definition = TraitFunctionsNode(
                        definitions = listOf(
                            TraitFunctionNode(
                                name = "sum",
                                type = FunctionTypeNode(
                                    params = listOf(
                                        ParameterTypeNode(
                                            name = "a",
                                            location = Location.NoProvided
                                        ),
                                        ParameterTypeNode(
                                            name = "a",
                                            location = Location.NoProvided
                                        ),
                                        ParameterTypeNode(name = "a", location = Location.NoProvided)
                                    ),
                                    location = Location.NoProvided,
                                    locations = FunctionTypeNodeLocations(separators = listOf())
                                ),
                                location = Location.NoProvided,
                                locations = TraitFunctionNodeLocation(
                                    name = Location.NoProvided,
                                    operator = Location.NoProvided
                                )
                            ), TraitFunctionNode(
                                name = "prod",
                                type = FunctionTypeNode(
                                    params = listOf(
                                        ParameterTypeNode(
                                            name = "a",
                                            location = Location.NoProvided
                                        ), ParameterTypeNode(
                                            name = "a",
                                            location = Location.NoProvided
                                        ), ParameterTypeNode(name = "a", location = Location.NoProvided)
                                    ),
                                    location = Location.NoProvided,
                                    locations = FunctionTypeNodeLocations(separators = listOf())
                                ),
                                location = Location.NoProvided,
                                locations = TraitFunctionNodeLocation(
                                    name = Location.NoProvided,
                                    operator = Location.NoProvided
                                )
                            )
                        ), functions = listOf(
                            FunctionNode(
                                native = false,
                                pub = false,
                                annotations = null,
                                name = "sum",
                                parameters = listOf("a", "b"),
                                expression = OperatorNode(
                                    category = "Operator10",
                                    operator = "+",
                                    left = FunctionCallNode(
                                        name = "a",
                                        type = FunctionType.Function,
                                        arguments = listOf(),
                                        location = Location.NoProvided
                                    ),
                                    right = FunctionCallNode(
                                        name = "b",
                                        type = FunctionType.Function,
                                        arguments = listOf(),
                                        location = Location.NoProvided
                                    ),
                                    location = Location.NoProvided
                                ),
                                location = Location.NoProvided,
                                locations = FunctionNodeLocations(
                                    nativeLocation = Location.NoProvided,
                                    pubLocation = Location.NoProvided,
                                    name = Location.NoProvided,
                                    parameters = listOf(),
                                    assignOperator = Location.NoProvided
                                )
                            )
                        )
                    ),
                    location = Location.NoProvided,
                    locations = TraitNodeLocations(
                        internalLocation = Location.NoProvided,
                        traitLocation = Location.NoProvided,
                        name = Location.NoProvided,
                        params = listOf(),
                        assignOperatorLocation = Location.NoProvided
                    )
                )
            )
    }
    "Parsing a trait with default impl on a method with annotations" {
        """
            trait Num a =
                sum :: a -> a -> a
                prod :: a -> a -> a
                
                @native(lang="java")
                sum a b = a + b
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
                    params = listOf("a"),
                    definition = TraitFunctionsNode(
                        definitions = listOf(
                            TraitFunctionNode(
                                name = "sum",
                                type = FunctionTypeNode(
                                    params = listOf(
                                        ParameterTypeNode(
                                            name = "a",
                                            location = Location.NoProvided
                                        ),
                                        ParameterTypeNode(
                                            name = "a",
                                            location = Location.NoProvided
                                        ),
                                        ParameterTypeNode(name = "a", location = Location.NoProvided)
                                    ),
                                    location = Location.NoProvided,
                                    locations = FunctionTypeNodeLocations(separators = listOf())
                                ),
                                location = Location.NoProvided,
                                locations = TraitFunctionNodeLocation(
                                    name = Location.NoProvided,
                                    operator = Location.NoProvided
                                )
                            ), TraitFunctionNode(
                                name = "prod",
                                type = FunctionTypeNode(
                                    params = listOf(
                                        ParameterTypeNode(
                                            name = "a",
                                            location = Location.NoProvided
                                        ), ParameterTypeNode(
                                            name = "a",
                                            location = Location.NoProvided
                                        ), ParameterTypeNode(name = "a", location = Location.NoProvided)
                                    ),
                                    location = Location.NoProvided,
                                    locations = FunctionTypeNodeLocations(separators = listOf())
                                ),
                                location = Location.NoProvided,
                                locations = TraitFunctionNodeLocation(
                                    name = Location.NoProvided,
                                    operator = Location.NoProvided
                                )
                            )
                        ), functions = listOf(
                            FunctionNode(
                                native = false,
                                pub = false,
                                annotations = listOf(
                                    AnnotationNode(
                                        name = "native",
                                        attrs = mapOf("lang" to "java"),
                                        location = Location.NoProvided,
                                        locations = AnnotationNodeLocations(
                                            altLocation = Location.NoProvided,
                                            name = Location.NoProvided,
                                            attrs = emptyList()
                                        )
                                    )
                                ),
                                name = "sum",
                                parameters = listOf("a", "b"),
                                expression = OperatorNode(
                                    category = "Operator10",
                                    operator = "+",
                                    left = FunctionCallNode(
                                        name = "a",
                                        type = FunctionType.Function,
                                        arguments = listOf(),
                                        location = Location.NoProvided
                                    ),
                                    right = FunctionCallNode(
                                        name = "b",
                                        type = FunctionType.Function,
                                        arguments = listOf(),
                                        location = Location.NoProvided
                                    ),
                                    location = Location.NoProvided
                                ),
                                location = Location.NoProvided,
                                locations = FunctionNodeLocations(
                                    nativeLocation = Location.NoProvided,
                                    pubLocation = Location.NoProvided,
                                    name = Location.NoProvided,
                                    parameters = listOf(),
                                    assignOperator = Location.NoProvided
                                )
                            )
                        )
                    ),
                    location = Location.NoProvided,
                    locations = TraitNodeLocations(
                        internalLocation = Location.NoProvided,
                        traitLocation = Location.NoProvided,
                        name = Location.NoProvided,
                        params = listOf(),
                        assignOperatorLocation = Location.NoProvided
                    )
                )
            )
    }
    "Parsing a trait with inner annotations" {
        """
            trait Num a =
                sum :: a -> a -> a
                prod :: a -> a -> a
                
                @native(lang="java")
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
                    params = listOf("a"),
                    definition = TraitFunctionsNode(
                        definitions = listOf(
                            TraitFunctionNode(
                                name = "sum",
                                type = FunctionTypeNode(
                                    params = listOf(
                                        ParameterTypeNode(
                                            name = "a",
                                            location = Location.NoProvided
                                        ),
                                        ParameterTypeNode(
                                            name = "a",
                                            location = Location.NoProvided
                                        ),
                                        ParameterTypeNode(name = "a", location = Location.NoProvided)
                                    ),
                                    location = Location.NoProvided,
                                    locations = FunctionTypeNodeLocations(separators = listOf())
                                ),
                                location = Location.NoProvided,
                                locations = TraitFunctionNodeLocation(
                                    name = Location.NoProvided,
                                    operator = Location.NoProvided
                                )
                            ), TraitFunctionNode(
                                name = "prod",
                                type = FunctionTypeNode(
                                    params = listOf(
                                        ParameterTypeNode(
                                            name = "a",
                                            location = Location.NoProvided
                                        ), ParameterTypeNode(
                                            name = "a",
                                            location = Location.NoProvided
                                        ), ParameterTypeNode(name = "a", location = Location.NoProvided)
                                    ),
                                    location = Location.NoProvided,
                                    locations = FunctionTypeNodeLocations(separators = listOf())
                                ),
                                location = Location.NoProvided,
                                locations = TraitFunctionNodeLocation(
                                    name = Location.NoProvided,
                                    operator = Location.NoProvided
                                )
                            )
                        ), functions = listOf()
                    ),
                    location = Location.NoProvided,
                    locations = TraitNodeLocations(
                        internalLocation = Location.NoProvided,
                        traitLocation = Location.NoProvided,
                        name = Location.NoProvided,
                        params = listOf(),
                        assignOperatorLocation = Location.NoProvided
                    )
                )
            )
    }
    "Parsing trait with definition methods" {
        """
            trait Sum a =
              sum :: a -> a -> a
              
              sum a b = a + b
              mul a b = a * b
        """.trimIndent()
            .also { println(it) }
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value.also { println(it) } }
            .shouldBeRight(
                TraitNode(
                    internal = false, annotations = null, name = "Sum", params = listOf("a"),
                    definition = TraitFunctionsNode(
                        definitions = listOf(
                            TraitFunctionNode(
                                name = "sum",
                                type = FunctionTypeNode(
                                    params = listOf(
                                        ParameterTypeNode(name = "a", location = Location.NoProvided),
                                        ParameterTypeNode(name = "a", location = Location.NoProvided),
                                        ParameterTypeNode(name = "a", location = Location.NoProvided)
                                    ),
                                    location = Location.NoProvided,
                                    locations = FunctionTypeNodeLocations(separators = listOf())
                                ),
                                location = Location.NoProvided,
                                locations = TraitFunctionNodeLocation(
                                    name = Location.NoProvided,
                                    operator = Location.NoProvided
                                )
                            )
                        ),
                        functions = listOf(
                            FunctionNode(
                                native = false,
                                pub = false,
                                annotations = null,
                                name = "sum",
                                parameters = listOf("a", "b"),
                                expression = OperatorNode(
                                    category = "Operator10", operator = "+",
                                    left = FunctionCallNode(
                                        name = "a", type = FunctionType.Function, arguments = listOf(),
                                        location = Location.NoProvided
                                    ),
                                    right = FunctionCallNode(
                                        name = "b", type = FunctionType.Function, arguments = listOf(),
                                        location = Location.NoProvided
                                    ), location = Location.NoProvided
                                ),
                                location = Location.NoProvided,
                                locations = FunctionNodeLocations(
                                    nativeLocation = Location.NoProvided,
                                    pubLocation = Location.NoProvided,
                                    name = Location.NoProvided, parameters = listOf(),
                                    assignOperator = Location.NoProvided
                                )
                            ),
                            FunctionNode(
                                native = false,
                                pub = false,
                                annotations = null,
                                name = "mul",
                                parameters = listOf("a", "b"),
                                expression = OperatorNode(
                                    category = "Operator11",
                                    operator = "*",
                                    left = FunctionCallNode(
                                        name = "a",
                                        type = FunctionType.Function,
                                        arguments = listOf(),
                                        location = Location.NoProvided
                                    ),
                                    right = FunctionCallNode(
                                        name = "b",
                                        type = FunctionType.Function,
                                        arguments = listOf(),
                                        location = Location.NoProvided
                                    ), location = Location.NoProvided
                                ),
                                location = Location.NoProvided,
                                locations = FunctionNodeLocations(
                                    nativeLocation = Location.NoProvided,
                                    pubLocation = Location.NoProvided,
                                    name = Location.NoProvided,
                                    parameters = listOf(),
                                    assignOperator = Location.NoProvided
                                )
                            )
                        )
                    ),
                    location = Location.NoProvided,
                    locations = TraitNodeLocations(
                        internalLocation = Location.NoProvided,
                        traitLocation = Location.NoProvided,
                        name = Location.NoProvided,
                        params = emptyList(),
                        assignOperatorLocation = Location.NoProvided
                    )
                )
            )
    }
    "Parsing a impl" {
        """
            impl Eq for Num =
                (=) a b = a == b
                (!=) a b = a != b
        """.trimIndent()
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeImpl()
            .map { it.value }
            .shouldBeRight(
                ImplNode(
                    traitName = "Eq",
                    forType = ConcreteTypeNode("Num", Location.NoProvided),
                    functions = listOf(
                        FunctionNode(
                            native = false,
                            pub = false,
                            annotations = null,
                            name = "(=)",
                            parameters = listOf("a", "b"),
                            expression = OperatorNode(
                                category = "Operator7", operator = "==",
                                left = FunctionCallNode(
                                    name = "a", type = FunctionType.Function, arguments = emptyList(),
                                    location = Location.NoProvided
                                ),
                                right = FunctionCallNode(
                                    name = "b", type = FunctionType.Function,
                                    arguments = emptyList(),
                                    location = Location.NoProvided
                                ), location = Location.NoProvided
                            ),
                            location = Location.NoProvided,
                            locations = FunctionNodeLocations(
                                nativeLocation = Location.NoProvided,
                                pubLocation = Location.NoProvided,
                                name = Location.NoProvided,
                                parameters = emptyList(),
                                assignOperator = Location.NoProvided
                            )
                        ),
                        FunctionNode(
                            native = false,
                            pub = false,
                            annotations = null,
                            name = "(!=)",
                            parameters = listOf("a", "b"),
                            expression = OperatorNode(
                                category = "Operator7", operator = "!=",
                                left = FunctionCallNode(
                                    name = "a", type = FunctionType.Function, arguments = emptyList(),
                                    location = Location.NoProvided
                                ),
                                right = FunctionCallNode(
                                    name = "b", type = FunctionType.Function,
                                    arguments = emptyList(),
                                    location = Location.NoProvided
                                ), location = Location.NoProvided
                            ),
                            location = Location.NoProvided,
                            locations = FunctionNodeLocations(
                                nativeLocation = Location.NoProvided,
                                pubLocation = Location.NoProvided,
                                name = Location.NoProvided,
                                parameters = emptyList(),
                                assignOperator = Location.NoProvided
                            )
                        )
                    ),
                    location = Location.NoProvided,
                    locations = ImplNodeLocations(
                        traitName = Location.NoProvided,
                        forKeyword = Location.NoProvided,
                        assignOperator = Location.NoProvided
                    )
                )
            )
    }
})
