package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Line
import org.ksharp.common.Location
import org.ksharp.common.Offset
import org.ksharp.common.cast
import org.ksharp.nodes.*
import org.ksharp.test.shouldBeRight

class ParserLocationsTest : StringSpec({
    "Annotation locations" {
        val innerLocations = AnnotationNodeLocations(
            altLocation = Location(
                Line(value = 1) to Offset(value = 46), Line(value = 1) to Offset(value = 47)
            ),
            name = Location(
                (Line(value = 1) to Offset(value = 47)), Line(value = 1) to Offset(value = 53)
            ),
            attrs = listOf(
                AttributeLocation(
                    null,
                    String::class.java,
                    valueLocation = Location(
                        (Line(value = 1) to Offset(value = 54)), Line(value = 1) to Offset(value = 62)
                    ),
                    operator = null
                )
            )
        )
        "@native(True for=[\"java\" \"c#\"] wire->internal=@native(\"String\") Flag=False)"
            .lexerModule(true)
            .emitLocations(true) {
                it.consumeBlock(KSharpLexerIterator::consumeAnnotation)
            }
            .map { it.value }
            .shouldBeRight(
                AnnotationNode(
                    name = "native",
                    mapOf(
                        "default" to true,
                        "for" to listOf("java", "c#"),
                        "wire->internal" to AnnotationNode(
                            name = "native",
                            attrs = mapOf("default" to "String"),
                            location = Location(
                                Line(value = 1) to Offset(value = 46), Line(value = 1) to Offset(value = 47)
                            ),
                            locations = innerLocations
                        ),
                        "Flag" to false
                    ),
                    location = Location((Line(value = 1) to Offset(value = 0)), Line(value = 1) to Offset(value = 1)),
                    locations = AnnotationNodeLocations(
                        altLocation = Location(
                            (Line(value = 1) to Offset(value = 0)),
                            Line(value = 1) to Offset(value = 1)
                        ),
                        name = Location((Line(value = 1) to Offset(value = 1)), Line(value = 1) to Offset(value = 7)),
                        attrs = listOf(
                            AttributeLocation(
                                null,
                                java.lang.Boolean::class.java,
                                Location(
                                    (Line(value = 1) to Offset(value = 8)), (Line(value = 1) to Offset(value = 12))
                                ),
                                operator = null
                            ),
                            AttributeLocation(
                                Location(
                                    (Line(value = 1) to Offset(value = 13)),
                                    Line(value = 1) to Offset(value = 16)
                                ),
                                List::class.java,
                                listOf(
                                    AttributeLocation(
                                        null,
                                        String::class.java,
                                        Location(
                                            Line(value = 1) to Offset(value = 18), Line(value = 1) to Offset(value = 24)
                                        ),
                                        null
                                    ),
                                    AttributeLocation(
                                        null, String::class.java,
                                        Location(
                                            Line(value = 1) to Offset(value = 25), Line(value = 1) to Offset(value = 29)
                                        ), null
                                    )
                                ),
                                operator = Location(
                                    (Line(value = 1) to Offset(value = 16)), Line(value = 1) to Offset(value = 17)
                                )
                            ),
                            AttributeLocation(
                                Location(
                                    (Line(value = 1) to Offset(value = 31)), Line(value = 1) to Offset(value = 45)
                                ),
                                value = AnnotationNode::class.java,
                                innerLocations,
                                operator = Location(
                                    (Line(value = 1) to Offset(value = 45)), Line(value = 1) to Offset(value = 46)
                                )
                            ),
                            AttributeLocation(
                                Location(
                                    (Line(value = 1) to Offset(value = 64)), Line(value = 1) to Offset(value = 68)
                                ),
                                java.lang.Boolean::class.java,
                                Location(
                                    (Line(value = 1) to Offset(value = 69)), Line(value = 1) to Offset(value = 74)
                                ),
                                operator = Location(
                                    (Line(value = 1) to Offset(value = 68)), Line(value = 1) to Offset(value = 69)
                                )
                            )
                        )
                    )
                )
            )
    }
    "Type node header locations" {
        "type ListOfInt a b = Int"
            .lexerModule(true)
            .emitLocations(true) {
                it.consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
            }.map { it.value.cast<TypeNode>().locations }
            .shouldBeRight(
                TypeNodeLocations(
                    internalLocation = Location.NoProvided,
                    typeLocation = Location(
                        (Line(value = 1) to Offset(value = 0)),
                        (Line(value = 1) to Offset(value = 4))
                    ),
                    name = Location((Line(value = 1) to Offset(value = 5)), (Line(value = 1) to Offset(value = 14))),
                    params = listOf(
                        Location((Line(value = 1) to Offset(value = 15)), (Line(value = 1) to Offset(value = 16))),
                        Location((Line(value = 1) to Offset(value = 17)), (Line(value = 1) to Offset(value = 18)))
                    ),
                    assignOperatorLocation = Location(
                        (Line(value = 1) to Offset(value = 19)), (Line(value = 1) to Offset(value = 20))
                    )
                )
            )

        "internal type ListOfInt a b = Int"
            .lexerModule(true)
            .emitLocations(true) {
                it.consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
            }.map { it.value.cast<TypeNode>().locations }
            .shouldBeRight(
                TypeNodeLocations(
                    internalLocation = Location(
                        (Line(value = 1) to Offset(value = 0)),
                        (Line(value = 1) to Offset(value = 8))
                    ),
                    typeLocation = Location(
                        (Line(value = 1) to Offset(value = 9)),
                        (Line(value = 1) to Offset(value = 13))
                    ),
                    name = Location((Line(value = 1) to Offset(value = 14)), (Line(value = 1) to Offset(value = 23))),
                    params = listOf(
                        Location((Line(value = 1) to Offset(value = 24)), (Line(value = 1) to Offset(value = 25))),
                        Location((Line(value = 1) to Offset(value = 26)), (Line(value = 1) to Offset(value = 27)))
                    ),
                    assignOperatorLocation = Location(
                        (Line(value = 1) to Offset(value = 28)), (Line(value = 1) to Offset(value = 29))
                    )
                )
            )
    }
    "Trait node header locations" {
        """
            trait Num a =
                sum :: a -> a -> a
                prod :: a -> a -> a
        """.trimIndent()
            .lexerModule(true)
            .emitLocations(true) {
                it.consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
            }.map { it.value.cast<TraitNode>().locations.also(::println) }
            .shouldBeRight(
                TraitNodeLocations(
                    internalLocation = Location.NoProvided,
                    traitLocation = Location(
                        (Line(value = 1) to Offset(value = 0)),
                        Line(value = 1) to Offset(value = 5)
                    ),
                    name = Location((Line(value = 1) to Offset(value = 6)), Line(value = 1) to Offset(value = 9)),
                    params = listOf(
                        Location(
                            (Line(value = 1) to Offset(value = 10)),
                            Line(value = 1) to Offset(value = 11)
                        )
                    ),
                    assignOperatorLocation = Location(
                        (Line(value = 1) to Offset(value = 12)), Line(value = 1) to Offset(value = 13)
                    )
                )
            )

        """
            internal trait Num a =
                sum :: a -> a -> a
                prod :: a -> a -> a
        """.trimIndent()
            .lexerModule(true)
            .emitLocations(true) {
                it.consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
            }.map { it.value.cast<TraitNode>().locations.also(::println) }
            .shouldBeRight(
                TraitNodeLocations(
                    internalLocation = Location(
                        (Line(value = 1) to Offset(value = 0)),
                        Line(value = 1) to Offset(value = 8)
                    ),
                    traitLocation = Location(
                        (Line(value = 1) to Offset(value = 9)),
                        Line(value = 1) to Offset(value = 14)
                    ),
                    name = Location((Line(value = 1) to Offset(value = 15)), Line(value = 1) to Offset(value = 18)),
                    params = listOf(
                        Location(
                            (Line(value = 1) to Offset(value = 19)),
                            Line(value = 1) to Offset(value = 20)
                        )
                    ),
                    assignOperatorLocation = Location(
                        (Line(value = 1) to Offset(value = 21)), Line(value = 1) to Offset(value = 22)
                    )
                )
            )
    }
    "Function type node locations" {
        "type ListOfInt = (List Int) -> a -> a -> b"
            .lexerModule(true)
            .emitLocations(true) {
                it.consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
            }.map {
                it.value.cast<TypeNode>()
                    .expr
                    .locations.also(::println)
            }
            .shouldBeRight(
                FunctionTypeNodeLocations(
                    separators = listOf(
                        Location((Line(value = 1) to Offset(value = 28)), Line(value = 1) to Offset(value = 30)),
                        Location((Line(value = 1) to Offset(value = 33)), Line(value = 1) to Offset(value = 35)),
                        Location((Line(value = 1) to Offset(value = 38)), Line(value = 1) to Offset(value = 40))
                    )
                )
            )
    }
    "Tuple type node locations" {
        "type Point = Double , Double, Int"
            .lexerModule(true)
            .emitLocations(true) {
                it.consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
            }.map {
                it.value.cast<TypeNode>()
                    .expr
                    .locations.also(::println)
            }
            .shouldBeRight(
                TupleTypeNodeLocations(
                    separators = listOf(
                        Location((Line(value = 1) to Offset(value = 20)), Line(value = 1) to Offset(value = 21)),
                        Location((Line(value = 1) to Offset(value = 28)), Line(value = 1) to Offset(value = 29))
                    )
                )
            )
    }
    "Union type node locations" {
        "type Bool = True | False | NoSet"
            .lexerModule(true)
            .emitLocations(true) {
                it.consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
            }.map {
                it.value.cast<TypeNode>()
                    .expr
                    .locations.also(::println)
            }
            .shouldBeRight(
                UnionTypeNodeLocations(
                    separators = listOf(
                        Location((Line(value = 1) to Offset(value = 17)), Line(value = 1) to Offset(value = 18)),
                        Location((Line(value = 1) to Offset(value = 25)), Line(value = 1) to Offset(value = 26))
                    )
                )
            )
    }
    "Intersection type node locations" {
        "type Bool = True & False & NoSet"
            .lexerModule(true)
            .emitLocations(true) {
                it.consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
            }.map {
                it.value.cast<TypeNode>()
                    .expr
                    .locations.also(::println)
            }
            .shouldBeRight(
                IntersectionTypeNodeLocations(
                    separators = listOf(
                        Location((Line(value = 1) to Offset(value = 17)), Line(value = 1) to Offset(value = 18)),
                        Location((Line(value = 1) to Offset(value = 25)), Line(value = 1) to Offset(value = 26))
                    )
                )
            )
    }
    "Constrained type node locations" {
        "type Age = Int => (it > 0) && (it < 70)"
            .lexerModule(true)
            .emitLocations(true) {
                it.consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
            }.map {
                it.value.cast<TypeNode>()
                    .expr
                    .locations.also(::println)
            }.shouldBeRight(
                ConstrainedTypeNodeLocations(
                    Location((Line(value = 1) to Offset(value = 15)), Line(value = 1) to Offset(value = 17))
                )
            )
    }
    "Trait functions node locations" {
        """
            trait Num a =
                sum :: a -> a -> a
                prod :: a -> a -> a
        """.trimIndent()
            .lexerModule(true)
            .emitLocations(true) {
                it.consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
            }.map {
                it.value.cast<TraitNode>().definition.functions.map {
                    it.locations
                }
            }
            .shouldBeRight(
                listOf(
                    TraitFunctionNodeLocation(
                        Location((Line(value = 2) to Offset(value = 4)), Line(value = 2) to Offset(value = 7)),
                        Location((Line(value = 2) to Offset(value = 8)), Line(value = 2) to Offset(value = 10))
                    ),
                    TraitFunctionNodeLocation(
                        Location((Line(value = 3) to Offset(value = 4)), Line(value = 3) to Offset(value = 8)),
                        Location((Line(value = 3) to Offset(value = 9)), Line(value = 3) to Offset(value = 11))
                    )
                )
            )
    }
    "Type declaration node locations" {
        "sum a :: (Num a) -> (Num a) -> Int"
            .lexerModule(true)
            .emitLocations(true) {
                it.consumeBlock(KSharpLexerIterator::consumeFunctionTypeDeclaration)
            }.map {
                it.value.cast<TypeDeclarationNode>().locations
            }.shouldBeRight(
                TypeDeclarationNodeLocations(
                    Location((Line(value = 1) to Offset(value = 0)), Line(value = 1) to Offset(value = 3)),
                    Location((Line(value = 1) to Offset(value = 6)), Line(value = 1) to Offset(value = 8)),
                    listOf(Location((Line(value = 1) to Offset(value = 4)), Line(value = 1) to Offset(value = 5)))
                )
            )
    }
    "Import node locations" {
        "import ksharp.math as math"
            .lexerModule(true)
            .emitLocations(true) {
                it.consumeBlock(KSharpLexerIterator::consumeImport)
            }.map {
                it.value.cast<ImportNode>().locations.also(::println)
            }.shouldBeRight(
                ImportNodeLocations(
                    Location((Line(value = 1) to Offset(value = 0)), Line(value = 1) to Offset(value = 6)),
                    Location((Line(value = 1) to Offset(value = 7)), Line(value = 1) to Offset(value = 13)),
                    Location((Line(value = 1) to Offset(value = 14)), Line(value = 1) to Offset(value = 18)),
                    Location((Line(value = 1) to Offset(value = 19)), Line(value = 1) to Offset(value = 21)),
                    Location((Line(value = 1) to Offset(value = 22)), Line(value = 1) to Offset(value = 26)),
                )
            )
    }
    "Import node locations 2" {
        "import ksharp as math"
            .lexerModule(true)
            .emitLocations(true) {
                it.consumeBlock(KSharpLexerIterator::consumeImport)
            }.map {
                it.value.cast<ImportNode>().locations.also(::println)
            }.shouldBeRight(
                ImportNodeLocations(
                    Location((Line(value = 1) to Offset(value = 0)), Line(value = 1) to Offset(value = 6)),
                    Location((Line(value = 1) to Offset(value = 7)), Line(value = 1) to Offset(value = 13)),
                    Location((Line(value = 1) to Offset(value = 7)), Line(value = 1) to Offset(value = 13)),
                    Location((Line(value = 1) to Offset(value = 14)), Line(value = 1) to Offset(value = 16)),
                    Location((Line(value = 1) to Offset(value = 17)), Line(value = 1) to Offset(value = 21)),
                )
            )
    }
    "Native function node locations" {
        "native sum a b"
            .lexerModule(true)
            .emitLocations(true) {
                it.consumeBlock(KSharpLexerIterator::consumeFunction)
            }.map {
                it.value.cast<FunctionNode>().locations.also(::println)
            }.shouldBeRight(
                FunctionNodeLocations(
                    Location((Line(value = 1) to Offset(value = 0)), Line(value = 1) to Offset(value = 6)),
                    Location.NoProvided,
                    Location((Line(value = 1) to Offset(value = 7)), Line(value = 1) to Offset(value = 10)),
                    listOf(
                        Location((Line(value = 1) to Offset(value = 11)), Line(value = 1) to Offset(value = 12)),
                        Location((Line(value = 1) to Offset(value = 13)), Line(value = 1) to Offset(value = 14)),
                    ),
                    Location.NoProvided,
                )
            )
    }
    "Native pub function node locations" {
        "native pub sum a b"
            .lexerModule(true)
            .emitLocations(true) {
                it.consumeBlock(KSharpLexerIterator::consumeFunction)
            }.map {
                it.value.cast<FunctionNode>().locations.also(::println)
            }.shouldBeRight(
                FunctionNodeLocations(
                    Location((Line(value = 1) to Offset(value = 0)), Line(value = 1) to Offset(value = 6)),
                    Location((Line(value = 1) to Offset(value = 7)), Line(value = 1) to Offset(value = 10)),
                    Location((Line(value = 1) to Offset(value = 11)), Line(value = 1) to Offset(value = 14)),
                    listOf(
                        Location((Line(value = 1) to Offset(value = 15)), Line(value = 1) to Offset(value = 16)),
                        Location((Line(value = 1) to Offset(value = 17)), Line(value = 1) to Offset(value = 18)),
                    ),
                    Location.NoProvided,
                )
            )
    }
    "Native pub function node locations without arguments" {
        "native pub sum"
            .lexerModule(true)
            .emitLocations(true) {
                it.consumeBlock(KSharpLexerIterator::consumeFunction)
            }.map {
                it.value.cast<FunctionNode>().locations.also(::println)
            }.shouldBeRight(
                FunctionNodeLocations(
                    Location((Line(value = 1) to Offset(value = 0)), Line(value = 1) to Offset(value = 6)),
                    Location((Line(value = 1) to Offset(value = 7)), Line(value = 1) to Offset(value = 10)),
                    Location((Line(value = 1) to Offset(value = 11)), Line(value = 1) to Offset(value = 14)),
                    listOf(),
                    Location.NoProvided,
                )
            )
    }
    "function node locations" {
        "sum a b = a + b"
            .lexerModule(true)
            .emitLocations(true) {
                it.consumeBlock(KSharpLexerIterator::consumeFunction)
            }.map {
                it.value.cast<FunctionNode>().locations.also(::println)
            }.shouldBeRight(
                FunctionNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location((Line(value = 1) to Offset(value = 0)), Line(value = 1) to Offset(value = 3)),
                    listOf(
                        Location((Line(value = 1) to Offset(value = 4)), Line(value = 1) to Offset(value = 5)),
                        Location((Line(value = 1) to Offset(value = 6)), Line(value = 1) to Offset(value = 7)),
                    ),
                    Location((Line(value = 1) to Offset(value = 8)), Line(value = 1) to Offset(value = 9)),
                )
            )
    }
    "pub function node locations" {
        "pub ten = 10"
            .lexerModule(true)
            .emitLocations(true) {
                it.consumeBlock(KSharpLexerIterator::consumeFunction)
            }.map {
                it.value.cast<FunctionNode>().locations.also(::println)
            }.shouldBeRight(
                FunctionNodeLocations(
                    Location.NoProvided,
                    Location((Line(value = 1) to Offset(value = 0)), Line(value = 1) to Offset(value = 3)),
                    Location((Line(value = 1) to Offset(value = 4)), Line(value = 1) to Offset(value = 7)),
                    listOf(),
                    Location((Line(value = 1) to Offset(value = 8)), Line(value = 1) to Offset(value = 9)),
                )
            )
    }
    "if then else node locations" {
        "if 4 > a then 10 else 20"
            .lexerModule(true)
            .emitLocations(true) {
                it.consumeBlock(KSharpLexerIterator::consumeExpression)
            }.map {
                it.value.cast<IfNode>().locations.also(::println)
            }.shouldBeRight(
                IfNodeLocations(
                    Location((Line(value = 1) to Offset(value = 0)), Line(value = 1) to Offset(value = 2)),
                    Location((Line(value = 1) to Offset(value = 9)), Line(value = 1) to Offset(value = 13)),
                    Location((Line(value = 1) to Offset(value = 17)), Line(value = 1) to Offset(value = 21)),
                )
            )
    }
    "if then node locations" {
        "if 4 > a then 10"
            .lexerModule(true)
            .emitLocations(true) {
                it.consumeBlock(KSharpLexerIterator::consumeExpression)
            }.map {
                it.value.cast<IfNode>().locations.also(::println)
            }.shouldBeRight(
                IfNodeLocations(
                    Location((Line(value = 1) to Offset(value = 0)), Line(value = 1) to Offset(value = 2)),
                    Location((Line(value = 1) to Offset(value = 9)), Line(value = 1) to Offset(value = 13)),
                    Location.NoProvided,
                )
            )
    }
    "map literal node locations" {
        "{\"key1\": 1}"
            .lexerModule(true)
            .emitLocations(true) {
                it.consumeBlock(KSharpLexerIterator::consumeExpression)
            }.map {
                it.value.cast<LiteralCollectionNode>()
                    .values.first().cast<LiteralMapEntryNode>()
                    .locations.also(::println)
            }.shouldBeRight(
                LiteralMapEntryNodeLocations(
                    Location((Line(value = 1) to Offset(value = 7)), Line(value = 1) to Offset(value = 8))
                )
            )
    }
    "let match assignment  expression node locations" {
        """let x = 10
           |   y = 20
           |then x + y
        """.trimMargin()
            .lexerModule(true)
            .emitLocations(true) {
                it.consumeBlock { l ->
                    l.enableDiscardBlockAndNewLineTokens(KSharpLexerIterator::consumeExpression)
                }
            }.map {
                it.value.cast<LetExpressionNode>()
                    .matches.last()
                    .location.also(::println)
            }.shouldBeRight(
                Location((Line(value = 2) to Offset(value = 5)), Line(value = 2) to Offset(value = 6))
            )
    }
    "let expression node locations" {
        """let x = 10
           |   y = 20
           |then x + y
        """.trimMargin()
            .lexerModule(true)
            .emitLocations(true) {
                it.consumeBlock { l ->
                    l.enableDiscardBlockAndNewLineTokens(KSharpLexerIterator::consumeExpression)
                }
            }.map {
                it.value.cast<LetExpressionNode>()
                    .locations.also(::println)
            }.shouldBeRight(
                LetExpressionNodeLocations(
                    Location((Line(value = 1) to Offset(value = 0)), Line(value = 1) to Offset(value = 3)),
                    Location((Line(value = 3) to Offset(value = 0)), Line(value = 3) to Offset(value = 4))
                )
            )
    }
    "let list assignment expression node locations" {
        """let x = 10
           |   [(1, 2) | rest] = 20
           |then x + y
        """.trimMargin()
            .lexerModule(true)
            .emitLocations(true) {
                it.consumeBlock { l ->
                    l.enableDiscardBlockAndNewLineTokens(KSharpLexerIterator::consumeExpression)
                }
            }.map {
                it.value.cast<LetExpressionNode>()
                    .matches.last()
                    .match.cast<MatchListValueNode>()
                    .locations.also(::println)
            }.shouldBeRight(
                MatchListValueNodeLocations(
                    Location((Line(value = 2) to Offset(value = 11)), Line(value = 2) to Offset(value = 12)),
                )
            )
    }
})
