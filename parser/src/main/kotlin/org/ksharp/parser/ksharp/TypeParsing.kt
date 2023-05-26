package org.ksharp.parser.ksharp

import org.ksharp.common.add
import org.ksharp.common.addAll
import org.ksharp.common.cast
import org.ksharp.common.listBuilder
import org.ksharp.nodes.*
import org.ksharp.parser.*

private enum class SetType {
    Invalid,
    Union,
    Intersection
}


private fun KSharpLexerIterator.consumeTypeVariable() =
    consume({
        it.type == KSharpTokenType.LowerCaseWord || it.type == KSharpTokenType.UpperCaseWord
    })

private fun KSharpConsumeResult.thenIfTypeValueSeparator(block: (KSharpConsumeResult) -> KSharpConsumeResult) =
    thenIf({
        when {
            it.type == KSharpTokenType.Operator10 && it.text == "->" -> true
            it.type == KSharpTokenType.Comma -> true
            else -> false
        }
    }, false, block)

private fun KSharpLexerIterator.consumeTypeSetSeparator() =
    consume({
        when {
            it.type == KSharpTokenType.Operator4 && it.text == "|" -> true
            it.type == KSharpTokenType.Operator6 && it.text == "&" -> true
            else -> false
        }
    })

private fun Token.toTypeExpression(): TypeExpression =
    if (type == KSharpTokenType.UpperCaseWord)
        ConcreteTypeNode(text, location)
    else ParameterTypeNode(text, location)

private fun List<Any>.toValueTypes(token: Token?): NodeData {
    val hasLabel = token?.type == KSharpTokenType.Label
    val valueTypes = listBuilder<TypeExpression>()
    asSequence()
        .drop(if (hasLabel) 1 else 0)
        .forEach { item ->
            if (item is TypeExpression) {
                if (item is ParametricTypeNode) valueTypes.addAll(item.variables)
                else valueTypes.add(item)
                return@forEach
            }
            val result = (item as Token)
                .toTypeExpression()
            valueTypes.add(result)
        }

    val result = valueTypes.build()
    val firstNode = result.first().cast<NodeData>()

    return if (result.size == 1) firstNode
    else ParametricTypeNode(result, firstNode.location)
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

private fun List<Any>.toLabelOrValueType(): NodeData {
    val node = first()
    val type = this.toValueTypes(if (node is Token) node.cast<Token>() else null)
    return if (node is Token && node.type == KSharpTokenType.Label) {
        LabelTypeNode(
            node.text.let { t -> t.substring(0, t.length - 1) },
            type.cast<TypeExpression>(),
            node.location
        )
    } else type
}

private fun KSharpConsumeResult.thenJoinType() =
    appendNode {
        it.toLabelOrValueType()
    }.thenIfTypeValueSeparator { i ->
        i.consume { it.consumeTypeValue(true) }
    }.build {
        if (it.size == 1) return@build it.first().cast()
        val separator = it[1] as Token
        if (separator.type == KSharpTokenType.Operator10) {
            return@build it.toFunctionType(separator)
        }
        it.toTupleType(separator)
    }

private fun KSharpLexerIterator.consumeTypeValue(allowLabel: Boolean): KSharpParserResult =
    ifConsume(KSharpTokenType.OpenParenthesis, true) {
        it.consume { i -> i.consumeTypeValue(true) }
            .then(KSharpTokenType.CloseParenthesis, true)
            .let { l ->
                if (allowLabel) l.thenJoinType()
                else l.build { items ->
                    items.toLabelOrValueType()
                }
            }
    }.or {
        it.ifConsume(KSharpTokenType.UnitValue) { l ->
            l.build { i -> UnitTypeNode(i.first().cast<Token>().location).cast<NodeData>() }
                .let { result ->
                    if (allowLabel) result.resume()
                        .thenJoinType()
                    else result
                }
        }
    }.let { result ->
        if (allowLabel) {
            result.or {
                it.ifConsume(KSharpTokenType.Label) { l ->
                    l.consume {
                        consumeTypeValue(false)
                    }.build { items -> items.toLabelOrValueType() }
                        .resume()
                        .thenLoop { i -> i.consumeTypeValue(true) }
                        .thenJoinType()
                }
            }.or {
                it.consumeTypeVariable()
                    .thenLoop { i -> i.consumeTypeValue(true) }
                    .thenJoinType()
            }
        } else result.or {
            it.consumeTypeVariable()
                .build { items -> items.toLabelOrValueType() }
        }
    }

private fun List<Any>.calculateSetType(): SetType {
    var setType: SetType? = null
    for (element in asSequence().drop(1)) {
        element as SetElement
        val type = if (element.union) SetType.Union else SetType.Intersection
        if (setType == null) {
            setType = type
            continue
        }
        if (setType != type) {
            return SetType.Invalid
        }
    }
    return setType!!
}

private fun List<Any>.asTypeExpressionList(): List<TypeExpression> =
    map {
        if (it is SetElement) it.expression
        else it as TypeExpression
    }

private fun KSharpLexerIterator.consumeTypeExpr(): KSharpParserResult =
    consumeTypeValue(true)
        .resume()
        .thenLoop {
            it.consumeTypeSetSeparator()
                .consume { i -> i.consumeTypeValue(true) }
                .build { i ->
                    val setOperator = i.first() as Token
                    SetElement(
                        setOperator.type == KSharpTokenType.Operator4,
                        i.last().cast(),
                        setOperator.location
                    )
                }
        }.build {
            if (it.size == 1) it.first().cast()
            else {
                val setType = it.calculateSetType()
                val item = it.first() as NodeData
                when (setType) {
                    SetType.Invalid -> InvalidSetTypeNode(item.location)
                    SetType.Union -> UnionTypeNode(it.asTypeExpressionList(), item.location)
                    SetType.Intersection -> IntersectionTypeNode(it.asTypeExpressionList(), item.location)
                }
            }
        }

private fun KSharpLexerIterator.consumeTraitFunction(): KSharpParserResult =
    consumeLowerCaseWord()
        .then(KSharpTokenType.Operator, "::", true)
        .consume { it.consumeTypeValue(true) }
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
            }.resume()
                .thenIf(KSharpTokenType.Operator7, "=>", true) { l ->
                    l.consume { it.consumeExpression() }
                }.build {
                    val type = it.first().cast<TypeNode>()
                    if (it.size == 1) type.cast<NodeData>()
                    else {
                        val expr = it.last().cast<NodeData>()
                        type.copy(expr = ConstrainedTypeNode(type.expr.cast(), expr, expr.location))
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

internal fun KSharpLexerIterator.consumeFunctionTypeDeclaration(): KSharpParserResult =
    lookAHead {
        it.consumeFunctionName()
            .thenLoop { p ->
                p.consumeLowerCaseWord()
                    .build { param -> param.last().cast<LexerValue>().text }
            }
            .then(KSharpTokenType.Operator, "::", true)
            .enableDiscardBlockAndNewLineTokens { lx ->
                lx.consume { l -> l.consumeTypeValue(true) }
            }.build { i ->
                val name = i.first().cast<Token>()
                val params = i.asSequence()
                    .drop(1)
                    .takeWhile { v -> v !is NodeData }
                    .map { v -> v as String }
                TypeDeclarationNode(
                    name.text,
                    params.toList(),
                    i.last().cast(),
                    name.location
                ).cast<NodeData>()
            }.asLookAHeadResult()
    }
