package org.ksharp.parser.ksharp

import org.ksharp.common.add
import org.ksharp.common.cast
import org.ksharp.nodes.AnnotationNode
import org.ksharp.nodes.AnnotationNodeLocations
import org.ksharp.parser.*

typealias KSharpAnnotationValueResult = ParserResult<Any, KSharpLexerState>

private val String.isBooleanLiteral
    get() = when (this) {
        "True" -> true
        "False" -> true
        else -> false
    }

private fun KSharpAnnotationValueResult.orAnnotationValue(
    type: KSharpTokenType,
    createValue: (result: KSharpConsumeResult) -> KSharpAnnotationValueResult
): KSharpAnnotationValueResult =
    or { it.ifConsume(type, false, createValue) }

private fun KSharpAnnotationValueResult.orAnnotationValue(
    type: (token: Token) -> Boolean,
    createValue: (result: KSharpConsumeResult) -> KSharpAnnotationValueResult
): KSharpAnnotationValueResult =
    or { it.ifConsume(type, false, createValue) }

private fun KSharpLexerIterator.consumeAnnotationValue(): KSharpAnnotationValueResult =
    ifConsume(KSharpTokenType.Alt, false) {
        it.thenAnnotation().cast<KSharpAnnotationValueResult>()
    }.orAnnotationValue(KSharpTokenType.String) {
        it.build { l ->
            l.first().cast<Token>().text.let { t ->
                t.substring(1, t.length - 1)
            }
        }
    }.orAnnotationValue(KSharpTokenType.MultiLineString) {
        it.build { l ->
            l.first().cast<Token>().text.let { t ->
                t.substring(3, t.length - 3)
            }
        }
    }.orAnnotationValue({
        it.type == KSharpTokenType.UpperCaseWord && it.text.isBooleanLiteral
    }) { it.build { l -> l.first().cast<Token>().text == "True" } }
        .orAnnotationValue(KSharpTokenType.OpenBracket) {
            it.thenLoop { tl -> tl.consumeAnnotationValue() }
                .then(KSharpTokenType.CloseBracket, true)
                .build { v -> v.drop(1) }
        }

private fun KSharpLexerIterator.consumeAnnotationKeyValue(): KSharpAnnotationValueResult =
    ifConsume({
        when (it.type) {
            KSharpTokenType.LowerCaseWord -> true
            KSharpTokenType.FunctionName -> true
            KSharpTokenType.UpperCaseWord -> !it.text.isBooleanLiteral
            else -> false
        }
    }, false) { l ->
        l.thenAssignOperator()
            .consume {
                it.consumeAnnotationValue()
            }.build {
                val key = it.first().cast<Token>().text
                val value = it.last()
                (key to value).cast<Any>()
            }
    }.or {
        it.consumeAnnotationValue()
    }

private fun KSharpConsumeResult.thenAnnotation(): KSharpParserResult =
    then(KSharpTokenType.LowerCaseWord)
        .thenIf(KSharpTokenType.OpenParenthesis, true) {
            it.thenLoop { itAttr ->
                itAttr.consumeAnnotationKeyValue()
            }.then(KSharpTokenType.CloseParenthesis, true)
        }.thenIf(KSharpTokenType.UnitValue) { it }
        .build {
            val altToken = it[0].cast<Token>()
            val annotationName = it[1].cast<Token>()
            AnnotationNode(
                annotationName.text,
                it.asSequence()
                    .drop(1)
                    .filter { i -> i !is Token }
                    .associate { i ->
                        if (i is Pair<*, *>) i
                        else "default" to i
                    }.cast(),
                annotationName.location,
                AnnotationNodeLocations(altToken.location, annotationName.location, listOf())
            )
        }.map {
            it.remainTokens.state.value.annotations.update { list ->
                list.add(it.value)
            }
            it
        }.cast()

internal fun KSharpLexerIterator.consumeAnnotation(): KSharpParserResult =
    ifConsume(KSharpTokenType.Alt, false) { l ->
        l.enableDiscardBlockAndNewLineTokens { dbL ->
            dbL.disableCollapseAssignOperatorRule {
                it.thenAnnotation()
            }
        }
    }
