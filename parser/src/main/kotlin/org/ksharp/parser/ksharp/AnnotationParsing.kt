package org.ksharp.parser.ksharp

import org.ksharp.common.Location
import org.ksharp.common.add
import org.ksharp.common.cast
import org.ksharp.common.listBuilder
import org.ksharp.nodes.AnnotationNode
import org.ksharp.nodes.AnnotationNodeLocations
import org.ksharp.nodes.AttributeLocation
import org.ksharp.parser.*

private typealias KSharpAnnotationKeyValueResult = ParserResult<AnnotationKeyValue, KSharpLexerState>
private typealias KSharpAnnotationValueResult = ParserResult<Any, KSharpLexerState>

private data class AnnotationKeyValue(
    val key: String,
    val keyLocation: Location?,
    val value: Any,
    val assignment: Location?,
)

private val String.isBooleanLiteral
    get() = when (this) {
        "True" -> true
        "False" -> true
        else -> false
    }

private fun KSharpAnnotationValueResult.orAnnotationValue(
    type: KSharpTokenType,
    createValue: (result: KSharpConsumeResult) -> KSharpAnnotationValueResult = { it.build { l -> l.first() } }
): KSharpAnnotationValueResult =
    or { it.ifConsume(type, false, createValue) }

private fun KSharpAnnotationValueResult.orAnnotationValue(
    type: (token: Token) -> Boolean,
    createValue: (result: KSharpConsumeResult) -> KSharpAnnotationValueResult = { it.build { l -> l.first() } }
): KSharpAnnotationValueResult =
    or { it.ifConsume(type, false, createValue) }

private fun KSharpLexerIterator.consumeAnnotationValue(): KSharpAnnotationValueResult =
    ifConsume(KSharpTokenType.Alt, false) {
        it.thenAnnotation(this.state.value.emitLocations).cast<KSharpAnnotationValueResult>()
    }.orAnnotationValue(KSharpTokenType.String)
        .orAnnotationValue(KSharpTokenType.MultiLineString)
        .orAnnotationValue({
            it.type == KSharpTokenType.UpperCaseWord && it.text.isBooleanLiteral
        }).orAnnotationValue(KSharpTokenType.OpenBracket) {
            it.thenLoop { tl -> tl.consumeAnnotationValue() }
                .then(KSharpTokenType.CloseBracket, true)
                .build { v -> v.drop(1) }
        }

private fun KSharpLexerIterator.consumeAnnotationKeyValue(): KSharpAnnotationKeyValueResult =
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
                val key = it.first().cast<Token>()
                val assingOp = it[1].cast<Token>()
                val value = it.last()
                AnnotationKeyValue(
                    key.text,
                    key.location,
                    value,
                    assingOp.location
                )
            }
    }.or {
        it.consumeAnnotationValue().map { v ->
            ParserValue(
                AnnotationKeyValue(
                    "default",
                    null,
                    v.value,
                    null
                ),
                v.remainTokens
            )
        }
    }

private fun Any.toAnnotationValue(): Any =
    when (this) {
        is Token -> when (this.type) {
            KSharpTokenType.String -> text.substring(1, text.length - 1)
            KSharpTokenType.MultiLineString -> text.substring(3, text.length - 3)
            KSharpTokenType.UpperCaseWord -> text == "True"
            else -> TODO()
        }

        is List<*> -> this.map { it!!.toAnnotationValue() }
        else -> this
    }

private fun Any.toAnnotationLocation(): Any =
    when (this) {
        is Token -> this.location

        is List<*> -> this.map { it!!.toAnnotationLocation() }
        is AnnotationNode -> this.locations
        else -> Location.NoProvided
    }

private fun KSharpConsumeResult.thenAnnotation(emitLocations: Boolean): KSharpParserResult =
    then(KSharpTokenType.LowerCaseWord)
        .thenIf(KSharpTokenType.OpenParenthesis, true) {
            it.thenLoop { itAttr ->
                itAttr.consumeAnnotationKeyValue()
            }.then(KSharpTokenType.CloseParenthesis, true)
        }.thenIf(KSharpTokenType.UnitValue, true) { it }
        .build {
            val altToken = it[0].cast<Token>()
            val annotationName = it[1].cast<Token>()
            val attrsLocations = listBuilder<AttributeLocation>()
            AnnotationNode(
                annotationName.text,
                it.asSequence()
                    .drop(2)
                    .associate { i ->
                        val kv = i.cast<AnnotationKeyValue>()
                        if (emitLocations) {
                            attrsLocations.add(
                                AttributeLocation(
                                    kv.keyLocation,
                                    kv.value.toAnnotationLocation(),
                                    kv.assignment,
                                )
                            )
                        }
                        kv.key to kv.value.toAnnotationValue()
                    }.cast(),
                altToken.location,
                AnnotationNodeLocations(altToken.location, annotationName.location, attrsLocations.build())
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
                it.thenAnnotation(this.state.value.emitLocations)
            }
        }
    }
