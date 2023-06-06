package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Line
import org.ksharp.common.Location
import org.ksharp.common.Offset
import org.ksharp.nodes.AnnotationNode
import org.ksharp.nodes.AnnotationNodeLocations
import org.ksharp.nodes.AttributeLocation
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
                                operator = null
                            ),
                            AttributeLocation(
                                key = Location(context = "file.ks", position = (Line(value = 1) to Offset(value = 13))),
                                value = listOf(
                                    Location(
                                        context = "file.ks",
                                        position = (Line(value = 1) to Offset(value = 18))
                                    ), Location(context = "file.ks", position = (Line(value = 1) to Offset(value = 25)))
                                ),
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
})
