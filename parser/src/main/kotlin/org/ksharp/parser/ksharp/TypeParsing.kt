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

fun <L : LexerValue> Iterator<L>.consumeTypeVariable() =
    consume({
        it.type == KSharpTokenType.LowerCaseWord || it.type == KSharpTokenType.UpperCaseWord
    })

fun <L : LexerValue> ConsumeResult<L>.thenTypeVariable() =
    then({
        it.type == KSharpTokenType.LowerCaseWord || it.type == KSharpTokenType.UpperCaseWord
    }, {
        BaseParserErrorCode.ExpectingToken.new("token" to "<Word>", "received-token" to "${it.type}:${it.text}")
    })

fun <L : LexerValue> ConsumeResult<L>.thenIfTypeValueSeparator(block: (ConsumeResult<L>) -> ConsumeResult<L>) =
    thenIf({
        when {
            it.type == KSharpTokenType.Operator3 && it.text == "->" -> true
            it.type == KSharpTokenType.Comma -> true
            else -> false
        }
    }, false, block)

fun <L : LexerValue> Iterator<L>.consumeTypeSetSeparator() =
    consume({
        when {
            it.type == KSharpTokenType.Operator9 && it.text == "|" -> true
            it.type == KSharpTokenType.Operator7 && it.text == "&" -> true
            else -> false
        }
    })

private fun <L : LexerValue> ConsumeResult<L>.thenJoinType() =
    thenIfTypeValueSeparator { i ->
        i.consume { it.consumeTypeValue() }
    }.build { it.toTypeValue() }

fun <L : LexerValue> Iterator<L>.consumeTypeValue(): KSharpParserResult<L> =
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

fun <L : LexerValue> Iterator<L>.consumeTypeExpr(): KSharpParserResult<L> =
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

fun <L : LexerValue> Iterator<L>.consumeTraitFunction(): KSharpParserResult<L> =
    consumeLowerCaseWord()
        .then(KSharpTokenType.Operator, "::", true)
        .consume { it.consumeTypeValue() }
        .endExpression()
        .build { it.toTypeValue() }

fun <L : LexerValue> Iterator<L>.consumeTrait(internal: Boolean): KSharpParserResult<L> =
    consumeKeyword("trait")
        .thenUpperCaseWord()
        .thenLoop {
            it.consumeLowerCaseWord()
                .build { param ->
                    param.last().cast<LexerValue>().text
                }
        }.thenAssignOperator()
        .thenLoop {
            it.consumeTraitFunction()
        }
        .build { it.toType(internal) }

fun <L : LexerValue> ConsumeResult<L>.consumeType(internal: Boolean): KSharpParserResult<L> =
    thenKeyword("type")
        .thenUpperCaseWord()
        .thenLoop {
            it.consumeLowerCaseWord()
                .build { param ->
                    param.last().cast<LexerValue>().text
                }
        }
        .thenAssignOperator()
        .consume { it.consumeTypeExpr() }
        .endExpression()
        .build { it.toType(internal) }
        .or {
            it.consumeTrait(internal)
        }

fun <L : LexerValue> Iterator<L>.consumeTypeDeclaration(): KSharpParserResult<L> =
    ifConsume(KSharpTokenType.LowerCaseWord, "internal", true) {
        it.consumeType(true)
    }.or { it.collect().consumeType(false) }