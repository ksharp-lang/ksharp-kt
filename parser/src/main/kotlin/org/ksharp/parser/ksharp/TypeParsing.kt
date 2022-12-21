package org.ksharp.parser.ksharp

import org.ksharp.common.cast
import org.ksharp.nodes.TempNode
import org.ksharp.parser.*

fun List<Any>.toInternalType() = toPublicType()
fun List<Any>.toPublicType() = TempNode(this.map { if (it is LexerValue) it.cast<LexerValue>().text else it })

fun List<Any>.toTypeValue() = TempNode(this.map { if (it is LexerValue) it.cast<LexerValue>().text else it })

fun <L : LexerValue> Iterator<L>.consumeTypeVariable() =
    consume({
        it.type == KSharpTokenType.LowerCaseWord || it.type == KSharpTokenType.UpperCaseWord
    })

fun <L : LexerValue> Iterator<L>.consumeTypeSeparator() =
    consume({
        when {
            it.type == KSharpTokenType.Operator3 && it.text == "->" -> true
            it.type == KSharpTokenType.Operator2 && it.text == "*" -> true
            it.type == KSharpTokenType.Operator2 && it.text == "," -> true
            it.type == KSharpTokenType.Operator9 && it.text == "|" -> true
            it.type == KSharpTokenType.Operator7 && it.text == "&" -> true
            else -> false
        }
    })


private fun <L : LexerValue> ConsumeResult<L>.consumeSubTypes(): KSharpParserResult<L> =
    thenLoop {
        it.consumeTypeValue()
    }.build {
        it.toTypeValue()
    }

private fun <L : LexerValue> Iterator<L>.consumeTypeValue(): KSharpParserResult<L> =
    consumeTypeVariable()
        .consumeSubTypes()

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
        .consume { it.consumeTypeValue() }
        .thenLoop {
            it.consumeTypeSeparator()
                .consume { lexer -> lexer.consumeTypeValue() }
                .build { data -> data.toTypeValue() }
        }
        .build { if (internal) it.toInternalType() else it.toPublicType() }

fun <L : LexerValue> Iterator<L>.consumeTypeDeclaration(): KSharpParserResult<L> =
    consumeKeyword("internal", true)
        .consumeType(true)
        .or { collect().consumeType(false) }