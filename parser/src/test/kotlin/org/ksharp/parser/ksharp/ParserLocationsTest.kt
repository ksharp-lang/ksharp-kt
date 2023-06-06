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
                context = "file.ks",
                position = (Line(value = 1) to Offset(value = 46))
            ),
            name = Location(
                context = "file.ks",
                position = (Line(value = 1) to Offset(value = 47))
            ),
            attrs = listOf(
                AttributeLocation(
                    null,
                    value = Location(
                        context = "file.ks",
                        position = (Line(value = 1) to Offset(value = 54))
                    ),
                    valueLength = 8,
                    operator = null
                )
            )
        )
        "@native(True for=[\"java\" \"c#\"] wire->internal=@native(\"String\") Flag=False)"
            .lexerModule("file.ks", true)
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
                                context = "file.ks",
                                position = (Line(value = 1) to Offset(value = 47))
                            ),
                            locations = innerLocations
                        ),
                        "Flag" to false
                    ),
                    location = Location(context = "file.ks", position = (Line(value = 1) to Offset(value = 1))),
                    locations = AnnotationNodeLocations(
                        altLocation = Location(context = "file.ks", position = (Line(value = 1) to Offset(value = 0))),
                        name = Location(context = "file.ks", position = (Line(value = 1) to Offset(value = 1))),
                        attrs = listOf(
                            AttributeLocation(
                                key = null,
                                value = Location(
                                    context = "file.ks",
                                    position = (Line(value = 1) to Offset(value = 8))
                                ),
                                valueLength = 4,
                                operator = null
                            ),
                            AttributeLocation(
                                key = Location(context = "file.ks", position = (Line(value = 1) to Offset(value = 13))),
                                value = listOf(
                                    Location(
                                        context = "file.ks",
                                        position = (Line(value = 1) to Offset(value = 18))
                                    ) to 6,
                                    Location(
                                        context = "file.ks",
                                        position = (Line(value = 1) to Offset(value = 25))
                                    ) to 4
                                ),
                                valueLength = 0,
                                operator = Location(
                                    context = "file.ks",
                                    position = (Line(value = 1) to Offset(value = 16))
                                )
                            ),
                            AttributeLocation(
                                key = Location(
                                    context = "file.ks",
                                    position = (Line(value = 1) to Offset(value = 31))
                                ),
                                value = innerLocations,
                                valueLength = 0,
                                operator = Location(
                                    context = "file.ks",
                                    position = (Line(value = 1) to Offset(value = 45))
                                )
                            ),
                            AttributeLocation(
                                key = Location(
                                    context = "file.ks",
                                    position = (Line(value = 1) to Offset(value = 64))
                                ),
                                value = Location(
                                    context = "file.ks",
                                    position = (Line(value = 1) to Offset(value = 69))
                                ),
                                valueLength = 5,
                                operator = Location(
                                    context = "file.ks",
                                    position = (Line(value = 1) to Offset(value = 68))
                                )
                            )
                        )
                    )
                )
            )
    }
    "Type node header locations" {
        "type ListOfInt a b = Int"
            .lexerModule("file.ks", true)
            .emitLocations(true) {
                it.consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
            }.map { it.value.cast<TypeNode>().locations }
            .shouldBeRight(
                TypeNodeLocations(
                    internalLocation = Location.NoProvided,
                    typeLocation = Location(context = "file.ks", position = (Line(value = 1) to Offset(value = 0))),
                    name = Location(context = "file.ks", position = (Line(value = 1) to Offset(value = 5))),
                    params = listOf(
                        Location(context = "file.ks", position = (Line(value = 1) to Offset(value = 15))),
                        Location(context = "file.ks", position = (Line(value = 1) to Offset(value = 17)))
                    ),
                    assignOperatorLocation = Location(
                        context = "file.ks",
                        position = (Line(value = 1) to Offset(value = 19))
                    )
                )
            )

        "internal type ListOfInt a b = Int"
            .lexerModule("file.ks", true)
            .emitLocations(true) {
                it.consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
            }.map { it.value.cast<TypeNode>().locations }
            .shouldBeRight(
                TypeNodeLocations(
                    internalLocation = Location(context = "file.ks", position = (Line(value = 1) to Offset(value = 0))),
                    typeLocation = Location(context = "file.ks", position = (Line(value = 1) to Offset(value = 9))),
                    name = Location(context = "file.ks", position = (Line(value = 1) to Offset(value = 14))),
                    params = listOf(
                        Location(context = "file.ks", position = (Line(value = 1) to Offset(value = 24))),
                        Location(context = "file.ks", position = (Line(value = 1) to Offset(value = 26)))
                    ),
                    assignOperatorLocation = Location(
                        context = "file.ks",
                        position = (Line(value = 1) to Offset(value = 28))
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
            .lexerModule("file.ks", true)
            .emitLocations(true) {
                it.consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
            }.map { it.value.cast<TraitNode>().locations.also(::println) }
            .shouldBeRight(
                TraitNodeLocations(
                    internalLocation = Location.NoProvided,
                    traitLocation = Location(context = "file.ks", position = (Line(value = 1) to Offset(value = 0))),
                    name = Location(context = "file.ks", position = (Line(value = 1) to Offset(value = 6))),
                    params = listOf(Location(context = "file.ks", position = (Line(value = 1) to Offset(value = 10)))),
                    assignOperatorLocation = Location(
                        context = "file.ks",
                        position = (Line(value = 1) to Offset(value = 12))
                    )
                )
            )

        """
            internal trait Num a =
                sum :: a -> a -> a
                prod :: a -> a -> a
        """.trimIndent()
            .lexerModule("file.ks", true)
            .emitLocations(true) {
                it.consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
            }.map { it.value.cast<TraitNode>().locations.also(::println) }
            .shouldBeRight(
                TraitNodeLocations(
                    internalLocation = Location(context = "file.ks", position = (Line(value = 1) to Offset(value = 0))),
                    traitLocation = Location(context = "file.ks", position = (Line(value = 1) to Offset(value = 9))),
                    name = Location(context = "file.ks", position = (Line(value = 1) to Offset(value = 15))),
                    params = listOf(Location(context = "file.ks", position = (Line(value = 1) to Offset(value = 19)))),
                    assignOperatorLocation = Location(
                        context = "file.ks",
                        position = (Line(value = 1) to Offset(value = 21))
                    )
                )

            )
    }
})
