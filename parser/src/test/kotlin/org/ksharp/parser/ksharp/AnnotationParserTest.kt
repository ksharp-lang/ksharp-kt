package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Location
import org.ksharp.nodes.AnnotationNode
import org.ksharp.nodes.AnnotationNodeLocations
import org.ksharp.parser.TokenLexerIterator
import org.ksharp.parser.enableLookAhead
import org.ksharp.test.shouldBeRight

private fun TokenLexerIterator<KSharpLexerState>.prepareLexerForAnnotationParsing() =
    filterAndCollapseTokens()
        .collapseNewLines()
        .enableIndentationOffset()
        .enableLookAhead()


class AnnotationParserTest : StringSpec({
    "Annotation without attributes" {
        "@native"
            .kSharpLexer()
            .prepareLexerForAnnotationParsing()
            .consumeAnnotation()
            .map { it.value.also { println(it) } }
            .shouldBeRight(
                AnnotationNode(
                    "native",
                    mapOf(),
                    Location.NoProvided,
                    AnnotationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
                )
            )
    }
    "Annotation without attributes 2" {
        "@native()"
            .kSharpLexer()
            .prepareLexerForAnnotationParsing()
            .consumeAnnotation()
            .map { it.value.also { println(it) } }
            .shouldBeRight(
                AnnotationNode(
                    "native",
                    mapOf(),
                    Location.NoProvided,
                    AnnotationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
                )
            )
    }
    "Annotation with attributes" {
        "@native(True for=[\"java\" \"c#\"] wire->internal=@native(\"String\") Flag=False)"
            .kSharpLexer()
            .prepareLexerForAnnotationParsing()
            .consumeAnnotation()
            .map { it.value.also { println(it) } }
            .shouldBeRight(
                AnnotationNode(
                    "native", mapOf(
                        "default" to true,
                        "for" to listOf("java", "c#"),
                        "wire->internal" to AnnotationNode(
                            "native", mapOf("default" to "String"), Location.NoProvided, AnnotationNodeLocations(
                                Location.NoProvided, Location.NoProvided, listOf()
                            )
                        ),
                        "Flag" to false
                    ), Location.NoProvided, AnnotationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
                )
            )
    }
    "Annotation with attributes and indentation" {
        """
        @native(
            for=True
            Flag=False)
        """.trimIndent()
            .kSharpLexer()
            .prepareLexerForAnnotationParsing()
            .consumeAnnotation()
            .map { it.value.also { println(it) } }
            .shouldBeRight(
                AnnotationNode(
                    "native", mapOf(
                        "for" to true,
                        "Flag" to false
                    ), Location.NoProvided, AnnotationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
                )
            )
    }
    "Annotation with attributes and indentation 2" {
        """
        @native(True for=[
                          "java" 
                          "c#"] 
                      wire->internal=@native("String") Flag=False)
        """.trimIndent()
            .kSharpLexer()
            .prepareLexerForAnnotationParsing()
            .consumeAnnotation()
            .map { it.value.also { println(it) } }
            .shouldBeRight(
                AnnotationNode(
                    "native", mapOf(
                        "default" to true,
                        "for" to listOf("java", "c#"),
                        "wire->internal" to AnnotationNode(
                            "native", mapOf("default" to "String"), Location.NoProvided, AnnotationNodeLocations(
                                Location.NoProvided, Location.NoProvided, listOf()
                            )
                        ),
                        "Flag" to false
                    ), Location.NoProvided, AnnotationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
                )
            )
    }
})
