package org.ksharp.parser.ksharp

import org.ksharp.common.Either
import org.ksharp.common.Location
import org.ksharp.common.add
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

private fun KSharpConsumeResult.thenRepeatingFunCallIndentation(): KSharpConsumeResult =
    flatMap { nc ->
        val lexer = nc.tokens
        val indentationOffset = lexer.state.value.indentationOffset
        lexer.withNextTokenIndentationOffset(OffsetType.Repeating) {
            val offset = indentationOffset.currentOffset
            val tokenPredicate: (Token) -> Boolean = { tk ->
                tk.type == BaseTokenType.NewLine && indentationOffset.currentOffset == offset
            }
            Either.Right(nc)
                .thenLoop { l ->
                    l.ifConsume(tokenPredicate, true) { iL ->
                        iL.consume { c ->
                            c.consumeExpression(true)
                        }.lastNodeData()
                    }.or { o ->
                        o.consumeExpressionValue(tupleWithoutParenthesis = true, withBindings = true)
                    }
                }
        }
    }

fun KSharpLexerIterator.consumeFunctionCall(): KSharpParserResult =
    ifConsume({
        when (it.type) {
            KSharpTokenType.OperatorFunctionName -> true
            KSharpTokenType.FunctionName -> true
            KSharpTokenType.LowerCaseWord -> true
            KSharpTokenType.UpperCaseWord -> true
            else -> false
        }
    }) { l ->
        l.thenRepeatingFunCallIndentation().build {
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
    }

fun KSharpLexerIterator.consumeIfExpression(): KSharpParserResult =
    ifConsume(KSharpTokenType.If, false) { ifLexer ->
        ifLexer
            .consume { l -> l.consumeExpression() }
            .then(KSharpTokenType.Then, false)
            .consume { l -> l.consumeExpression() }
            .thenIf(KSharpTokenType.Else, false) { el ->
                el.consume { l -> l.consumeExpression() }
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
    }

fun KSharpLexerIterator.consumeMatchExpression(): KSharpParserResult =
    ifConsume(KSharpTokenType.Match, false) { matchLexer ->
        matchLexer.consume { it.consumeExpression() }
            .then(KSharpTokenType.With, false)
            .thenRepeatingIndentation(true) { t ->
                t.consume {
                    it.consumeMatchExpressionBranch()
                }.lastNodeData()
            }.build {
                val matchToken = it.first().cast<Token>()
                val expr = it[1].cast<NodeData>()
                val withToken = it[2].cast<Token>()
                val branches = it.drop(3).cast<List<MatchExpressionBranchNode>>()
                MatchExpressionNode(
                    expr, branches, matchToken.location,
                    MatchExpressionNodeLocations(matchToken.location, withToken.location)
                )
            }
    }

fun KSharpLexerIterator.consumeLetExpression(): KSharpParserResult =
    ifConsume(KSharpTokenType.Let, false) { letLexer ->
        letLexer
            .withAlignedIndentationOffset(IndentationOffsetType.StartOffset) {
                it.thenRepeatingIndentation(false) { l ->
                    l.consume { cl -> cl.consumeMatchAssignment() }
                        .build { i -> i.first().cast() }
                }
            }.then(KSharpTokenType.Then, false)
            .consume { it.consumeExpression() }
            .build {
                val letToken = it.first().cast<Token>()
                val expr = it.last().cast<NodeData>()
                val matches = it.asSequence().filterIsInstance<MatchAssignNode>().toList()
                LetExpressionNode(
                    matches, expr, letToken.location,
                    LetExpressionNodeLocations(letToken.location, it[it.size - 2].cast<Token>().location)
                )
            }
    }

internal fun KSharpLexerIterator.consumeExpressionValue(
    tupleWithoutParenthesis: Boolean = true,
    withBindings: Boolean = false
): KSharpParserResult {
    val groupExpression = ifConsume(KSharpTokenType.OpenParenthesis, true) {
        it.consume { l ->
            l.consumeExpression()
        }.then(KSharpTokenType.CloseParenthesis, true)
            .build { l ->
                l.first().cast<NodeData>()
            }
    }.or { l ->
        l.consumeLiteral(withBindings)
    }.or { it.consumeIfExpression() }
        .or { it.consumeLetExpression() }
        .or { it.consumeMatchExpression() }

    val withFunctionCall =
        if (!withBindings) groupExpression.or {
            it.consumeFunctionCall()
        } else groupExpression

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
            val category = operator.type.toString()
            val left = it.first().cast<NodeData>()
            val right = it.last().cast<NodeData>()
            if (right is OperatorNode && right.category == category) {
                OperatorNode(
                    category,
                    right.operator,
                    OperatorNode(
                        category,
                        operator.text,
                        left,
                        right.left,
                        operator.location,
                    ),
                    right.right,
                    operator.location,
                )
            } else OperatorNode(
                category,
                operator.text,
                left,
                right,
                operator.location,
            )
        }
    }

private fun KSharpParserResult.thenOperatorExpression(
    type: KSharpTokenType,
    block: (KSharpLexerIterator) -> KSharpParserResult
): KSharpParserResult =
    resume()
        .flatMap {
            val tokens = it.tokens
            val checkpoint = tokens.state.lookAHeadState.checkpoint()
            val token = if (tokens.hasNext()) tokens.next() else null
            if (token != null && token.type == type) {
                checkpoint.end(ConsumeTokens)
                it.collection.add(token)
                block(tokens).map { p ->
                    it.collection.add(p.value as Any)
                    NodeCollector(
                        it.collection,
                        p.remainTokens
                    )
                }
            } else {
                checkpoint.end(PreserveTokens)
                Either.Right(it)
            }
        }.buildOperatorExpression()

private fun KSharpLexerIterator.consumeOperator(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeExpressionValue(
        tupleWithoutParenthesis
    ).thenOperatorExpression(KSharpTokenType.Operator) {
        it.consumeOperator(
            tupleWithoutParenthesis
        )
    }


private fun KSharpLexerIterator.consumeOperator12(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeOperator(
        tupleWithoutParenthesis
    ).thenOperatorExpression(KSharpTokenType.Operator12) {
        it.consumeOperator12(
            tupleWithoutParenthesis
        )
    }

private fun KSharpLexerIterator.consumeOperator11(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeOperator12(
        tupleWithoutParenthesis
    ).thenOperatorExpression(KSharpTokenType.Operator11) {
        it.consumeOperator11(
            tupleWithoutParenthesis
        )
    }

private fun KSharpLexerIterator.consumeOperator10(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeOperator11(
        tupleWithoutParenthesis
    ).thenOperatorExpression(KSharpTokenType.Operator10) {
        it.consumeOperator10(
            tupleWithoutParenthesis
        )
    }

private fun KSharpLexerIterator.consumeOperator09(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeOperator10(
        tupleWithoutParenthesis
    ).thenOperatorExpression(KSharpTokenType.Operator9) {
        it.consumeOperator09(
            tupleWithoutParenthesis
        )
    }

private fun KSharpLexerIterator.consumeOperator08(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeOperator09(
        tupleWithoutParenthesis
    ).thenOperatorExpression(KSharpTokenType.Operator8) {
        it.consumeOperator08(
            tupleWithoutParenthesis
        )
    }

private fun KSharpLexerIterator.consumeOperator07(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeOperator08(
        tupleWithoutParenthesis
    ).thenOperatorExpression(KSharpTokenType.Operator7) {
        it.consumeOperator07(
            tupleWithoutParenthesis
        )
    }

private fun KSharpLexerIterator.consumeOperator06(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeOperator07(
        tupleWithoutParenthesis
    ).thenOperatorExpression(KSharpTokenType.Operator6) {
        it.consumeOperator06(
            tupleWithoutParenthesis
        )
    }

private fun KSharpLexerIterator.consumeOperator05(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeOperator06(
        tupleWithoutParenthesis
    ).thenOperatorExpression(KSharpTokenType.Operator5) {
        it.consumeOperator05(
            tupleWithoutParenthesis
        )
    }

private fun KSharpLexerIterator.consumeOperator04(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeOperator05(
        tupleWithoutParenthesis
    ).thenOperatorExpression(KSharpTokenType.Operator4) {
        it.consumeOperator04(
            tupleWithoutParenthesis
        )
    }

private fun KSharpLexerIterator.consumeOperator03(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeOperator04(
        tupleWithoutParenthesis
    ).thenOperatorExpression(KSharpTokenType.Operator3) {
        it.consumeOperator03(
            tupleWithoutParenthesis
        )
    }

private fun KSharpLexerIterator.consumeOperator02(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeOperator03(
        tupleWithoutParenthesis
    ).thenOperatorExpression(KSharpTokenType.Operator2) {
        it.consumeOperator02(
            tupleWithoutParenthesis
        )
    }

private fun KSharpLexerIterator.consumeOperator01(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeOperator02(
        tupleWithoutParenthesis
    ).thenOperatorExpression(KSharpTokenType.Operator1) {
        it.consumeOperator01(
            tupleWithoutParenthesis
        )
    }

private fun KSharpLexerIterator.consumeOperator0(
    tupleWithoutParenthesis: Boolean
): KSharpParserResult =
    consumeOperator01(
        tupleWithoutParenthesis
    ).thenOperatorExpression(KSharpTokenType.Operator0) {
        it.consumeOperator0(
            tupleWithoutParenthesis
        )
    }

fun KSharpLexerIterator.consumeExpression(
    tupleWithoutParenthesis: Boolean = true
): KSharpParserResult = withNextTokenIndentationOffset(OffsetType.Optional) {
    it.consumeOperator0(tupleWithoutParenthesis)
}
