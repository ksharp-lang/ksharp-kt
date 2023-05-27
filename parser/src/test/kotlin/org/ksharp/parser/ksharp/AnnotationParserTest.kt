package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Location
import org.ksharp.nodes.AnnotationNode
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
            .shouldBeRight(AnnotationNode("native", mapOf(), Location.NoProvided))
    }
    "Annotation without attributes 2" {
        "@native()"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeAnnotation)
            .map { it.value.also { println(it) } }
            .shouldBeRight(AnnotationNode("native", mapOf(), Location.NoProvided))
    }
    "Annotation with attributes" {
        "@native(True for = [\"java\" \"c#\"] test = @native(\"String\") flag = False)"
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
                        "test" to AnnotationNode("native", mapOf("default" to "String"), Location.NoProvided),
                        "flag" to false
                    ), Location.NoProvided
                )
            )
    }
})
