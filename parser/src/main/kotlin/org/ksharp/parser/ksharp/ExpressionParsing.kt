package org.ksharp.parser.ksharp

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.nodes.*
import org.ksharp.parser.*

private val Token.functionType
    get() =
        when (type) {
            KSharpTokenType.OperatorFunctionName -> FunctionType.Operator
            KSharpTokenType.UpperCaseWord -> FunctionType.TypeInstance
            else -> FunctionType.Function
        }

fun KSharpLexerIterator.consumeFunctionCall(): KSharpParserResult =
    consume({
        when (it.type) {
            KSharpTokenType.OperatorFunctionName -> true
            KSharpTokenType.FunctionName -> true
            KSharpTokenType.LowerCaseWord -> true
            KSharpTokenType.UpperCaseWord -> true
            else -> false
        }
    }).thenLoop {
        it.consumeExpressionValue(tupleWithoutParenthesis = true, withBindings = true)
    }
        .build {
            val fnName = it.first().cast<Token>()
            FunctionCallNode(
                fnName.text,
                fnName.functionType,
                it.drop(1).map { arg ->
                    if (arg is LiteralValueNode && arg.type == LiteralValueType.Binding && arg.value.endsWith(":")) {
                        arg.copy(type = LiteralValueType.Label)
                    } else arg as NodeData
                },
                fnName.location
            )
        }

fun KSharpLexerIterator.consumeIfExpression(): KSharpParserResult =
    consume(KSharpTokenType.If, false)
        .enableIfKeywords {
            it.consume { l -> l.consumeExpression() }
                .thenOptional(KSharpTokenType.NewLine, true)
                .then(KSharpTokenType.Then, false)
                .consume { l -> l.consumeExpression() }
                .thenOptional(KSharpTokenType.NewLine, true)
                .thenIf(KSharpTokenType.Else, false) { el ->
                    el.consume { l -> l.consumeExpression() }
                }
        }.build {
            val location = it.first().cast<Token>().location
            val locations = IfNodeLocations(
                it[0].cast<Token>().location,
                it[2].cast<Token>().location,
                if (it.size == 4) Location.NoProvided else it[4].cast<Token>().location
            )
            if (it.size == 4) IfNode(
                it[1] as NodeData,
                it[3] as NodeData,
                UnitNode(location),
                location,
                locations,
            ) else IfNode(
                it[1] as NodeData,
                it[3] as NodeData,
                it[5] as NodeData,
                location,
                locations
            )
        }

private fun KSharpConsumeResult.discardBlanks() =
    map {
        val lexer = it.tokens
        while (lexer.hasNext()) {
            val token = lexer.next()
            if (token.type == KSharpTokenType.NewLine || token.type == KSharpTokenType.EndBlock) {
                continue
            }
            return@map NodeCollector(
                it.collection,
                lexer.cons(token)
            )
        }
        it
    }

fun KSharpLexerIterator.consumeLetExpression(): KSharpParserResult =
    consume(KSharpTokenType.Let, false)
        .enableLetKeywords { l ->
            l.thenLoop {
                it.consumeMatchAssignment()
                    .resume()
                    .discardBlanks()
                    .build { items ->
                        items.first().cast<NodeData>()
                    }
            }.then(KSharpTokenType.Then, true)
                .consume { it.consumeExpression() }
        }.build {
            val letToken = it.first().cast<Token>()
            val expr = it.last().cast<NodeData>()
            val matches = it.asSequence().filterIsInstance<MatchAssignNode>().toList()
            LetExpressionNode(
                matches, expr, letToken.location
            )
        }

internal fun KSharpLexerIterator.consumeExpressionValue(
    tupleWithoutParenthesis: Boolean = true,
    withBindings: Boolean = false
): KSharpParserResult {
    val groupExpression = ifConsume(KSharpTokenType.OpenParenthesis, true) {
        it.consume { l ->
            l.enableExpressionStartingNewLine { nL ->
                nL.consumeExpression()
            }
        }.then(KSharpTokenType.CloseParenthesis, true)
            .build { l ->
                l.first().cast<NodeData>()
            }
    }

    val newLineExpression = if (state.value.enableExpressionStartingNewLine) {
        groupExpression.or {
            it.ifConsume(KSharpTokenType.NewLine, true) { ifL ->
                ifL.discardBlanks()
                    .consume { l -> l.consumeExpression() }
                    .build { l -> l.first().cast<NodeData>() }
            }
        }
    } else groupExpression

    val literal = newLineExpression.or {
        it.consumeLiteral(withBindings)
    }.or { it.consumeIfExpression() }
        .or { it.consumeLetExpression() }

    val withFunctionCall =
        if (!withBindings) literal.or {
            it.consumeFunctionCall()
        } else literal

    return if (tupleWithoutParenthesis) {
        withFunctionCall
            .resume()
            .thenLoop {
                it.consume(KSharpTokenType.Comma, true)
                    .consume { l -> l.consumeExpressionValue(false) }
                    .build { l -> l.first().cast() }
            }.build {
                if (it.size == 1) it.first().cast<NodeData>()
                else LiteralCollectionNode(
                    it.cast(), LiteralCollectionType.Tuple,
                    it.first().cast<NodeData>().location,
                )
            }
    } else withFunctionCall
}

private fun KSharpConsumeResult.buildOperatorExpression(): KSharpParserResult =
    build {
        if (it.size == 1) it.first().cast<NodeData>()
        else {
            val operator = it[1] as Token
            OperatorNode(
                operator.text,
                it.first().cast(),
                it.last().cast(),
                operator.location,
            )
        }
    }

private fun KSharpParserResult.thenOperatorRightExpression(
    type: KSharpTokenType,
    block: (KSharpLexerIterator) -> KSharpParserResult
): KSharpParserResult =
    resume().thenIf(type) { then ->
        then.consume(block)
    }.buildOperatorExpression()

private fun KSharpLexerIterator.consumeOperator12(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeOperator11(
        tupleWithoutParenthesis
    ).thenOperatorRightExpression(KSharpTokenType.Operator12) {
        it.consumeOperator12(
            tupleWithoutParenthesis
        )
    }


private fun KSharpLexerIterator.consumeOperator11(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeOperator10(
        tupleWithoutParenthesis
    ).thenOperatorRightExpression(KSharpTokenType.Operator11) {
        it.consumeOperator11(
            tupleWithoutParenthesis
        )
    }

private fun KSharpLexerIterator.consumeOperator10(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeOperator09(
        tupleWithoutParenthesis
    ).thenOperatorRightExpression(KSharpTokenType.Operator10) {
        it.consumeOperator10(
            tupleWithoutParenthesis
        )
    }

private fun KSharpLexerIterator.consumeOperator09(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeOperator08(
        tupleWithoutParenthesis
    ).thenOperatorRightExpression(KSharpTokenType.Operator9) {
        it.consumeOperator09(
            tupleWithoutParenthesis
        )
    }

private fun KSharpLexerIterator.consumeOperator08(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeOperator07(
        tupleWithoutParenthesis
    ).thenOperatorRightExpression(KSharpTokenType.Operator8) {
        it.consumeOperator08(
            tupleWithoutParenthesis
        )
    }

private fun KSharpLexerIterator.consumeOperator07(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeOperator06(
        tupleWithoutParenthesis
    ).thenOperatorRightExpression(KSharpTokenType.Operator7) {
        it.consumeOperator07(
            tupleWithoutParenthesis
        )
    }

private fun KSharpLexerIterator.consumeOperator06(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeOperator05(
        tupleWithoutParenthesis
    ).thenOperatorRightExpression(KSharpTokenType.Operator6) {
        it.consumeOperator06(
            tupleWithoutParenthesis
        )
    }

private fun KSharpLexerIterator.consumeOperator05(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeOperator04(
        tupleWithoutParenthesis
    ).thenOperatorRightExpression(KSharpTokenType.Operator5) {
        it.consumeOperator05(
            tupleWithoutParenthesis
        )
    }

private fun KSharpLexerIterator.consumeOperator04(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeOperator03(
        tupleWithoutParenthesis
    ).thenOperatorRightExpression(KSharpTokenType.Operator4) {
        it.consumeOperator04(
            tupleWithoutParenthesis
        )
    }

private fun KSharpLexerIterator.consumeOperator03(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeOperator02(
        tupleWithoutParenthesis
    ).thenOperatorRightExpression(KSharpTokenType.Operator3) {
        it.consumeOperator03(
            tupleWithoutParenthesis
        )
    }

private fun KSharpLexerIterator.consumeOperator02(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeOperator01(
        tupleWithoutParenthesis
    ).thenOperatorRightExpression(KSharpTokenType.Operator2) {
        it.consumeOperator02(
            tupleWithoutParenthesis
        )
    }

private fun KSharpLexerIterator.consumeOperator01(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeOperator0(
        tupleWithoutParenthesis
    ).thenOperatorRightExpression(KSharpTokenType.Operator1) {
        it.consumeOperator01(
            tupleWithoutParenthesis
        )
    }

private fun KSharpLexerIterator.consumeOperator0(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeOperator(
        tupleWithoutParenthesis
    ).thenOperatorRightExpression(KSharpTokenType.Operator0) {
        it.consumeOperator0(
            tupleWithoutParenthesis
        )
    }

private fun KSharpLexerIterator.consumeOperator(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeExpressionValue(
        tupleWithoutParenthesis
    ).thenOperatorRightExpression(KSharpTokenType.Operator) {
        it.consumeOperator(
            tupleWithoutParenthesis
        )
    }

fun KSharpLexerIterator.consumeExpression(
    tupleWithoutParenthesis: Boolean = true
): KSharpParserResult = consumeOperator12(tupleWithoutParenthesis)
