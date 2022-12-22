package org.ksharp.parser.ksharp

import org.ksharp.common.cast
import org.ksharp.nodes.NodeData
import org.ksharp.nodes.TempNode
import org.ksharp.parser.*

fun List<Any>.toInternalType() = toPublicType()
fun List<Any>.toPublicType() = TempNode(this.map { if (it is LexerValue) it.cast<LexerValue>().text else it })

fun List<Any>.toTypeValue() = TempNode(this.map { if (it is LexerValue) it.cast<LexerValue>().text else it })

fun <L : LexerValue> Iterator<L>.consumeTypeVariable() =
    consume({
        it.type == KSharpTokenType.LowerCaseWord || it.type == KSharpTokenType.UpperCaseWord
    })

fun <L : LexerValue> ConsumeResult<L>.thenIfTypeValueSeparator(block: (ConsumeResult<L>) -> ConsumeResult<L>) =
    thenIf({
        when {
            it.type == KSharpTokenType.Operator3 && it.text == "->" -> true
            it.type == KSharpTokenType.Operator2 && it.text == "*" -> true
            it.type == KSharpTokenType.Operator2 && it.text == "," -> true
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


private fun <L : LexerValue> Iterator<L>.consumeTypeValue(): KSharpParserResult<L> =
    ifConsume<NodeData, L>(KSharpTokenType.OpenParenthesis, true) {
        it.consume { i -> i.consumeTypeValue() }
            .then(KSharpTokenType.CloseParenthesis, true)
            .thenIfTypeValueSeparator { i ->
                i.consume { consumeTypeValue() }
            }
            .build { i ->
                i.toTypeValue()
            }
    }.or {
        it.consumeTypeVariable()
            .thenLoop { i -> i.consumeTypeValue() }
            .thenIfTypeValueSeparator { i ->
                i.consume { consumeTypeValue() }
            }
            .build { i -> i.toTypeValue() }
    }

fun <L : LexerValue> Iterator<L>.consumeTypeExpr(): KSharpParserResult<L> =
    consumeTypeValue()

fun <L : LexerValue> ConsumeResult<L>.consumeType(internal: Boolean): KSharpParserResult<L> =
    thenKeyword("type")
        .thenUpperCaseWord()
        .thenLoop {
            it.consumeLowerCaseWord()
                .build { param ->
                    param.last().cast<LexerValue>().text
                }
        }
        .thenAssignOperator(true)
        .consume { it.consumeTypeExpr() }
        .thenEndExpression()
        .build { if (internal) it.toInternalType() else it.toPublicType() }

fun <L : LexerValue> Iterator<L>.consumeTypeDeclaration(): KSharpParserResult<L> =
    consumeKeyword("internal")
        .consumeType(true)
        .or { it.collect().consumeType(false) }