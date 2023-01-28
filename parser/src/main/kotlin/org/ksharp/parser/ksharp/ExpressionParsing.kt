package org.ksharp.parser.ksharp

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
                it.drop(1).cast(),
                fnName.location
            )
        }

internal fun KSharpLexerIterator.consumeExpressionValue(
    tupleWithoutParenthesis: Boolean = true,
    withBindings: Boolean = false
): KSharpParserResult {
    val literal = ifConsume(KSharpTokenType.OpenParenthesis, true) {
        it.consume { l -> l.consumeExpression() }
            .then(KSharpTokenType.CloseParenthesis, true)
            .build { l ->
                l.first().cast<NodeData>()
            }
    }.or { it.consumeLiteral(withBindings) }

    val withTuple = if (tupleWithoutParenthesis) {
        literal
            .resume()
            .thenLoop {
                it.consume(KSharpTokenType.Comma, true)
                    .consume { l -> l.consumeExpressionValue(false) }
                    .build { l -> l.first().cast() }
            }.build {
                if (it.size == 1) it.first().cast<NodeData>()
                else LiteralCollectionNode(it.cast(), LiteralCollectionType.Tuple, it.first().cast<NodeData>().location)
            }
    } else literal

    return if (!withBindings) withTuple.or { it.consumeFunctionCall() } else withTuple
}

private fun KSharpConsumeResult.buildOperatorExpression(): KSharpParserResult =
    build {
        if (it.size == 1) it.first().cast<NodeData>()
        else {
            it.onEach(::println)
            val operator = it[1] as Token
            OperatorNode(
                operator.text,
                it.first().cast(),
                it.last().cast(),
                operator.location
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
    tupleWithoutParenthesis: Boolean = true,
    withBindings: Boolean = false
): KSharpParserResult =
    consumeOperator11(
        tupleWithoutParenthesis,
        withBindings
    ).thenOperatorRightExpression(KSharpTokenType.Operator12) {
        it.consumeOperator12(
            tupleWithoutParenthesis,
            withBindings
        )
    }


private fun KSharpLexerIterator.consumeOperator11(
    tupleWithoutParenthesis: Boolean = true,
    withBindings: Boolean = false
): KSharpParserResult =
    consumeOperator10(
        tupleWithoutParenthesis,
        withBindings
    ).thenOperatorRightExpression(KSharpTokenType.Operator11) {
        it.consumeOperator11(
            tupleWithoutParenthesis,
            withBindings
        )
    }

private fun KSharpLexerIterator.consumeOperator10(
    tupleWithoutParenthesis: Boolean = true,
    withBindings: Boolean = false
): KSharpParserResult =
    consumeOperator09(
        tupleWithoutParenthesis,
        withBindings
    ).thenOperatorRightExpression(KSharpTokenType.Operator10) {
        it.consumeOperator10(
            tupleWithoutParenthesis,
            withBindings
        )
    }

private fun KSharpLexerIterator.consumeOperator09(
    tupleWithoutParenthesis: Boolean = true,
    withBindings: Boolean = false
): KSharpParserResult =
    consumeOperator08(
        tupleWithoutParenthesis,
        withBindings
    ).thenOperatorRightExpression(KSharpTokenType.Operator9) {
        it.consumeOperator09(
            tupleWithoutParenthesis,
            withBindings
        )
    }

private fun KSharpLexerIterator.consumeOperator08(
    tupleWithoutParenthesis: Boolean = true,
    withBindings: Boolean = false
): KSharpParserResult =
    consumeOperator07(
        tupleWithoutParenthesis,
        withBindings
    ).thenOperatorRightExpression(KSharpTokenType.Operator8) {
        it.consumeOperator08(
            tupleWithoutParenthesis,
            withBindings
        )
    }

private fun KSharpLexerIterator.consumeOperator07(
    tupleWithoutParenthesis: Boolean = true,
    withBindings: Boolean = false
): KSharpParserResult =
    consumeOperator06(
        tupleWithoutParenthesis,
        withBindings
    ).thenOperatorRightExpression(KSharpTokenType.Operator7) {
        it.consumeOperator07(
            tupleWithoutParenthesis,
            withBindings
        )
    }

private fun KSharpLexerIterator.consumeOperator06(
    tupleWithoutParenthesis: Boolean = true,
    withBindings: Boolean = false
): KSharpParserResult =
    consumeOperator05(
        tupleWithoutParenthesis,
        withBindings
    ).thenOperatorRightExpression(KSharpTokenType.Operator6) {
        it.consumeOperator06(
            tupleWithoutParenthesis,
            withBindings
        )
    }

private fun KSharpLexerIterator.consumeOperator05(
    tupleWithoutParenthesis: Boolean = true,
    withBindings: Boolean = false
): KSharpParserResult =
    consumeOperator04(
        tupleWithoutParenthesis,
        withBindings
    ).thenOperatorRightExpression(KSharpTokenType.Operator5) {
        it.consumeOperator05(
            tupleWithoutParenthesis,
            withBindings
        )
    }

private fun KSharpLexerIterator.consumeOperator04(
    tupleWithoutParenthesis: Boolean = true,
    withBindings: Boolean = false
): KSharpParserResult =
    consumeOperator03(
        tupleWithoutParenthesis,
        withBindings
    ).thenOperatorRightExpression(KSharpTokenType.Operator4) {
        it.consumeOperator04(
            tupleWithoutParenthesis,
            withBindings
        )
    }

private fun KSharpLexerIterator.consumeOperator03(
    tupleWithoutParenthesis: Boolean = true,
    withBindings: Boolean = false
): KSharpParserResult =
    consumeOperator02(
        tupleWithoutParenthesis,
        withBindings
    ).thenOperatorRightExpression(KSharpTokenType.Operator3) {
        it.consumeOperator03(
            tupleWithoutParenthesis,
            withBindings
        )
    }

private fun KSharpLexerIterator.consumeOperator02(
    tupleWithoutParenthesis: Boolean = true,
    withBindings: Boolean = false
): KSharpParserResult =
    consumeOperator01(
        tupleWithoutParenthesis,
        withBindings
    ).thenOperatorRightExpression(KSharpTokenType.Operator2) {
        it.consumeOperator02(
            tupleWithoutParenthesis,
            withBindings
        )
    }

private fun KSharpLexerIterator.consumeOperator01(
    tupleWithoutParenthesis: Boolean = true,
    withBindings: Boolean = false
): KSharpParserResult =
    consumeOperator0(
        tupleWithoutParenthesis,
        withBindings
    ).thenOperatorRightExpression(KSharpTokenType.Operator1) {
        it.consumeOperator01(
            tupleWithoutParenthesis,
            withBindings
        )
    }

private fun KSharpLexerIterator.consumeOperator0(
    tupleWithoutParenthesis: Boolean = true,
    withBindings: Boolean = false
): KSharpParserResult =
    consumeOperator(
        tupleWithoutParenthesis,
        withBindings
    ).thenOperatorRightExpression(KSharpTokenType.Operator0) {
        it.consumeOperator0(
            tupleWithoutParenthesis,
            withBindings
        )
    }

private fun KSharpLexerIterator.consumeOperator(
    tupleWithoutParenthesis: Boolean = true,
    withBindings: Boolean = false
): KSharpParserResult =
    consumeExpressionValue(
        tupleWithoutParenthesis,
        withBindings
    ).thenOperatorRightExpression(KSharpTokenType.Operator) {
        it.consumeOperator(
            tupleWithoutParenthesis,
            withBindings
        )
    }

fun KSharpLexerIterator.consumeExpression(
    tupleWithoutParenthesis: Boolean = true,
    withBindings: Boolean = false
): KSharpParserResult = consumeOperator12(tupleWithoutParenthesis, withBindings)
