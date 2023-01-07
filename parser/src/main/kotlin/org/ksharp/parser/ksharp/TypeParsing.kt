package org.ksharp.parser.ksharp

import org.ksharp.common.cast
import org.ksharp.common.new
import org.ksharp.nodes.NodeData
import org.ksharp.nodes.TempNode
import org.ksharp.parser.*

fun List<Any>.toType(internal: Boolean): NodeData =
    if (internal) TempNode(listOf("internal", toType(false)))
    else TempNode(this.map { if (it is LexerValue) it.cast<LexerValue>().text else it })

fun List<Any>.toTypeValue(): NodeData = TempNode(this.map { if (it is LexerValue) it.cast<LexerValue>().text else it })

fun KSharpLexerIterator.consumeTypeVariable() =
    consume({
        it.type == KSharpTokenType.LowerCaseWord || it.type == KSharpTokenType.UpperCaseWord
    })

fun KSharpConsumeResult.thenTypeVariable() =
    then({
        it.type == KSharpTokenType.LowerCaseWord || it.type == KSharpTokenType.UpperCaseWord
    }, {
        BaseParserErrorCode.ExpectingToken.new("token" to "<Word>", "received-token" to "${it.type}:${it.text}")
    })

fun KSharpConsumeResult.thenIfTypeValueSeparator(block: (KSharpConsumeResult) -> KSharpConsumeResult) =
    thenIf({
        when {
            it.type == KSharpTokenType.Operator3 && it.text == "->" -> true
            it.type == KSharpTokenType.Comma -> true
            else -> false
        }
    }, false, block)

fun KSharpLexerIterator.consumeTypeSetSeparator() =
    consume({
        when {
            it.type == KSharpTokenType.Operator9 && it.text == "|" -> true
            it.type == KSharpTokenType.Operator7 && it.text == "&" -> true
            else -> false
        }
    })

private fun KSharpConsumeResult.thenJoinType() =
    thenIfTypeValueSeparator { i ->
        i.consume { it.consumeTypeValue() }
    }.build { it.toTypeValue() }

fun KSharpLexerIterator.consumeTypeValue(): KSharpParserResult =
    ifConsume(KSharpTokenType.OpenParenthesis, true) {
        it.consume { i -> i.consumeTypeValue() }
            .then(KSharpTokenType.CloseParenthesis, true)
            .thenJoinType()
    }.or {
        it.consume(KSharpTokenType.Label)
            .thenTypeVariable()
            .thenLoop { i -> i.consumeTypeValue() }
            .thenJoinType()
    }.or {
        it.consumeTypeVariable()
            .thenLoop { i -> i.consumeTypeValue() }
            .thenJoinType()
    }

fun KSharpLexerIterator.consumeTypeExpr(): KSharpParserResult =
    consumeTypeValue()
        .resume()
        .thenLoop {
            it.consumeTypeSetSeparator()
                .consume { i -> i.consumeTypeValue() }
                .build { i -> i.toTypeValue() }
        }.build {
            if (it.size == 1) it.first().cast()
            else it.toTypeValue()
        }

fun KSharpLexerIterator.consumeTraitFunction(): KSharpParserResult =
    consumeLowerCaseWord()
        .then(KSharpTokenType.Operator, "::", true)
        .consume { it.consumeTypeValue() }
        .thenNewLine()
        .build { it.toTypeValue() }

fun KSharpLexerIterator.consumeTrait(internal: Boolean): KSharpParserResult =
    consumeKeyword("trait")
        .enableDiscardBlockAndNewLineTokens { withoutBlocks ->
            withoutBlocks.thenUpperCaseWord()
                .thenLoop {
                    it.consumeLowerCaseWord()
                        .build { param -> param.last().cast<LexerValue>().text }
                }.thenAssignOperator()
        }.thenInBlock { block ->
            block.collect()
                .thenLoop { it.consumeTraitFunction() }
                .build { it.toTypeValue() }
        }
        .build { it.toType(internal) }

fun KSharpConsumeResult.consumeType(internal: Boolean): KSharpParserResult =
    thenKeyword("type")
        .enableDiscardBlockAndNewLineTokens { withoutBlocks ->
            withoutBlocks.enableLabelToken { withLabels ->
                withLabels.thenUpperCaseWord()
                    .thenLoop {
                        it.consumeLowerCaseWord()
                            .build { param ->
                                param.last().cast<LexerValue>().text
                            }
                    }
                    .thenAssignOperator()
                    .consume { it.consumeTypeExpr() }
                    .build { it.toType(internal) }
            }
        }.or {
            it.consumeTrait(internal)
        }

fun KSharpLexerIterator.consumeTypeDeclaration(): KSharpParserResult =
    ifConsume(KSharpTokenType.LowerCaseWord, "internal", true) {
        it.consumeType(true)
    }.or {
        it.collect().consumeType(false)
    }