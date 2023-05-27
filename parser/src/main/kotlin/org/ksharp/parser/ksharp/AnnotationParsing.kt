package org.ksharp.parser.ksharp

import org.ksharp.common.cast
import org.ksharp.nodes.AnnotationNode
import org.ksharp.parser.*

typealias KSharpAnnotationValueResult = ParserResult<Any, KSharpLexerState>

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

private fun KSharpConsumeResult.buildAnnotationValue(): KSharpAnnotationValueResult =
    build { it.first() }

private fun KSharpLexerIterator.consumeAnnotationValue(): KSharpAnnotationValueResult =
    ifConsume(KSharpTokenType.Alt) { it ->
        it.thenAnnotation().cast<KSharpAnnotationValueResult>()
    }.orAnnotationValue(KSharpTokenType.String) { it.buildAnnotationValue() }
        .orAnnotationValue({
            it.type == KSharpTokenType.UpperCaseWord && when (it.text) {
                "True" -> true
                "False" -> true
                else -> false
            }
        }) { it.buildAnnotationValue() }
        .orAnnotationValue(KSharpTokenType.OpenBracket) {
            it.thenLoop { tl -> tl.consumeAnnotationValue() }
                .then(KSharpTokenType.CloseBracket)
                .build { v -> v }
        }

private fun KSharpLexerIterator.consumeAnnotationKeyValue(): KSharpAnnotationValueResult =
    ifConsume(KSharpTokenType.LowerCaseWord) { l ->
        l.consume {
            it.consumeAnnotationValue()
        }.build {
            val key = it.first().cast<Token>().text
            val value = it.last()
            (key to value) as Any
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
            val annotationName = it.first().cast<Token>()
            AnnotationNode(
                annotationName.text,
                it.asSequence()
                    .drop(1)
                    .filter { i -> i !is Token }
                    .associate { i ->
                        if (i is Pair<*, *>) i
                        else "default" to i
                    }.cast(),
                annotationName.location
            )
        }

internal fun KSharpLexerIterator.consumeAnnotation(): KSharpParserResult =
    ifConsume(KSharpTokenType.Alt, true) {
        it.thenAnnotation()
    }
