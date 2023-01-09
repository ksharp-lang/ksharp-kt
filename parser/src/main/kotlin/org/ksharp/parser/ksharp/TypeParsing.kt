package org.ksharp.parser.ksharp

import org.ksharp.common.cast
import org.ksharp.common.new
import org.ksharp.nodes.*
import org.ksharp.parser.*

fun List<Any>.toTypeValue(): NodeData = TempNode(this.map { if (it is LexerValue) it.cast<LexerValue>().text else it })

private fun KSharpLexerIterator.consumeTypeVariable() =
    consume({
        it.type == KSharpTokenType.LowerCaseWord || it.type == KSharpTokenType.UpperCaseWord
    })

private fun KSharpConsumeResult.thenTypeVariable() =
    then({
        it.type == KSharpTokenType.LowerCaseWord || it.type == KSharpTokenType.UpperCaseWord
    }, {
        BaseParserErrorCode.ExpectingToken.new("token" to "<Word>", "received-token" to "${it.type}:${it.text}")
    })

private fun KSharpConsumeResult.thenIfTypeValueSeparator(block: (KSharpConsumeResult) -> KSharpConsumeResult) =
    thenIf({
        when {
            it.type == KSharpTokenType.Operator3 && it.text == "->" -> true
            it.type == KSharpTokenType.Comma -> true
            else -> false
        }
    }, false, block)

private fun KSharpLexerIterator.consumeTypeSetSeparator() =
    consume({
        when {
            it.type == KSharpTokenType.Operator9 && it.text == "|" -> true
            it.type == KSharpTokenType.Operator7 && it.text == "&" -> true
            else -> false
        }
    })

private fun Token.toTypeExpression(): TypeExpression =
    if (type == KSharpTokenType.UpperCaseWord)
        ConcreteTypeNode(text, location)
    else ParameterTypeNode(text, location)

private fun Token.toValueTypes(it: List<Any>): List<TypeExpression> {
    val hasLabel = this.type == KSharpTokenType.Label
    val valueTypes = mutableListOf<TypeExpression>()
    it.asSequence()
        .drop(if (hasLabel) 1 else 0)
        .forEach { item ->
            if (item is TypeExpression) {
                if (item is ParametricTypeNode) valueTypes.addAll(item.variables)
                else valueTypes.add(item)
                return@forEach
            }
            val result = (item as Token)
                .toTypeExpression()
                .let {
                    if (hasLabel) LabelTypeNode(
                        text.substring(0, text.length - 1),
                        it,
                        location
                    )
                    else it
                }
            valueTypes.add(result)
        }

    return valueTypes
}

private fun List<Any>.toFunctionType(separator: Token): NodeData {
    val first = first() as TypeExpression
    val last = last() as TypeExpression
    return if (last is FunctionTypeNode) {
        FunctionTypeNode(listOf(first) + last.params, separator.location)
    } else FunctionTypeNode(listOf(first, last), separator.location)
}

private fun List<Any>.toTupleType(separator: Token): NodeData {
    val first = first() as TypeExpression
    val last = last() as TypeExpression
    return if (last is TupleTypeNode) {
        TupleTypeNode(listOf(first) + last.types, separator.location)
    } else TupleTypeNode(listOf(first, last), separator.location)
}

private fun KSharpConsumeResult.thenJoinType() =
    appendNode {
        val node = it.first()
        if (node is NodeData) return@appendNode node
        node as Token
        val valueTypes = node.toValueTypes(it)
        if (valueTypes.size == 1) {
            valueTypes.first().cast()
        } else ParametricTypeNode(valueTypes.toList(), node.location)
    }.thenIfTypeValueSeparator { i ->
        i.consume { it.consumeTypeValue() }
    }.build {
        if (it.size == 1) return@build it.first().cast()
        val separator = it[1] as Token
        if (separator.type == KSharpTokenType.Operator3) {
            return@build it.toFunctionType(separator)
        }
        it.toTupleType(separator)
    }

private fun KSharpLexerIterator.consumeTypeValue(): KSharpParserResult =
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

private fun KSharpLexerIterator.consumeTypeExpr(): KSharpParserResult =
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

private fun KSharpLexerIterator.consumeTraitFunction(): KSharpParserResult =
    consumeLowerCaseWord()
        .then(KSharpTokenType.Operator, "::", true)
        .consume { it.consumeTypeValue() }
        .thenNewLine()
        .build {
            val name = it.first().cast<Token>()
            val type = it.last().cast<NodeData>()
            TraitFunctionNode(name.text, type, name.location)
        }

private fun KSharpLexerIterator.consumeTrait(internal: Boolean): KSharpParserResult =
    consumeKeyword("trait", true)
        .enableDiscardBlockAndNewLineTokens { withoutBlocks ->
            withoutBlocks.thenUpperCaseWord()
                .thenLoop {
                    it.consumeLowerCaseWord()
                        .build { param -> param.last().cast<LexerValue>().text }
                }.thenAssignOperator()
        }.thenInBlock { block ->
            block.collect()
                .thenLoop { it.consumeTraitFunction() }
                .build { TraitFunctionsNode(it.cast()) }
        }
        .build {
            val name = it.first().cast<LexerValue>()
            val params = if (it.size == 2) {
                listOf<String>()
            } else it.subList(1, it.size - 1).cast()
            TraitNode(internal, name.text, params, it.last().cast(), name.location)
                .cast()
        }

private fun KSharpConsumeResult.consumeType(internal: Boolean): KSharpParserResult =
    thenKeyword("type", true)
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
                    .build {
                        val name = it.first().cast<LexerValue>()
                        val params = if (it.size == 2) {
                            listOf<String>()
                        } else it.subList(1, it.size - 1).cast()
                        TypeNode(internal, name.text, params, it.last().cast(), name.location)
                            .cast<NodeData>()
                    }
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