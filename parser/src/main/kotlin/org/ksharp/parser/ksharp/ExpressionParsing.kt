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

private fun KSharpLexerIterator.consumeOperator12(): KSharpParserResult =
    consumeOperator11().thenOperatorRightExpression(KSharpTokenType.Operator12) { it.consumeOperator12() }


private fun KSharpLexerIterator.consumeOperator11(): KSharpParserResult =
    consumeOperator10().thenOperatorRightExpression(KSharpTokenType.Operator11) { it.consumeOperator11() }

private fun KSharpLexerIterator.consumeOperator10(): KSharpParserResult =
    consumeOperator09().thenOperatorRightExpression(KSharpTokenType.Operator10) { it.consumeOperator10() }

private fun KSharpLexerIterator.consumeOperator09(): KSharpParserResult =
    consumeOperator08().thenOperatorRightExpression(KSharpTokenType.Operator9) { it.consumeOperator09() }

private fun KSharpLexerIterator.consumeOperator08(): KSharpParserResult =
    consumeOperator07().thenOperatorRightExpression(KSharpTokenType.Operator8) { it.consumeOperator08() }

private fun KSharpLexerIterator.consumeOperator07(): KSharpParserResult =
    consumeOperator06().thenOperatorRightExpression(KSharpTokenType.Operator7) { it.consumeOperator07() }

private fun KSharpLexerIterator.consumeOperator06(): KSharpParserResult =
    consumeOperator05().thenOperatorRightExpression(KSharpTokenType.Operator6) { it.consumeOperator06() }

private fun KSharpLexerIterator.consumeOperator05(): KSharpParserResult =
    consumeOperator04().thenOperatorRightExpression(KSharpTokenType.Operator5) { it.consumeOperator05() }

private fun KSharpLexerIterator.consumeOperator04(): KSharpParserResult =
    consumeOperator03().thenOperatorRightExpression(KSharpTokenType.Operator4) { it.consumeOperator04() }

private fun KSharpLexerIterator.consumeOperator03(): KSharpParserResult =
    consumeOperator02().thenOperatorRightExpression(KSharpTokenType.Operator3) { it.consumeOperator03() }

private fun KSharpLexerIterator.consumeOperator02(): KSharpParserResult =
    consumeOperator01().thenOperatorRightExpression(KSharpTokenType.Operator2) { it.consumeOperator02() }

private fun KSharpLexerIterator.consumeOperator01(): KSharpParserResult =
    consumeOperator0().thenOperatorRightExpression(KSharpTokenType.Operator1) { it.consumeOperator01() }

private fun KSharpLexerIterator.consumeOperator0(): KSharpParserResult =
    consumeOperator().thenOperatorRightExpression(KSharpTokenType.Operator0) { it.consumeOperator0() }

private fun KSharpLexerIterator.consumeOperator(): KSharpParserResult =
    consumeExpressionValue().thenOperatorRightExpression(KSharpTokenType.Operator) { it.consumeOperator() }

fun KSharpLexerIterator.consumeExpression(): KSharpParserResult = consumeOperator12()