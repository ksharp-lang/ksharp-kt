package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Location
import org.ksharp.nodes.AnnotationNode
import org.ksharp.nodes.AnnotationNodeLocations
import org.ksharp.parser.LexerToken
import org.ksharp.parser.TextToken
import org.ksharp.test.shouldBeRight

class AnnotationParserTest : StringSpec({
    "Annotation without attributes" {
        "@native"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeAnnotation)
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
            .collapseKSharpTokens()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeAnnotation)
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
            .collapseKSharpTokens()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeAnnotation)
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
    "Annotation with attributes and block" {
        """
        @native(
            for=True
            Flag=False)
        """.trimIndent()
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeAnnotation)
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
})
