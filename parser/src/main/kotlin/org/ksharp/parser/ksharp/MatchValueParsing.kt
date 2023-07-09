package org.ksharp.parser.ksharp

import org.ksharp.common.cast
import org.ksharp.nodes.*
import org.ksharp.parser.*

private fun KSharpConsumeResult.buildConditionalMatchValueNode(type: MatchConditionalType): KSharpParserResult =
    build {
        if (it.size == 1) it.first().cast<NodeData>()
        else {
            val left = it.first().cast<NodeData>()
            val right = it.last().cast<NodeData>()
            val location = it[1].cast<Token>().location
            if (right is MatchConditionValueNode && right.type == type) {
                MatchConditionValueNode(
                    type,
                    MatchConditionValueNode(
                        type,
                        left,
                        right.left,
                        location
                    ),
                    right.right,
                    location
                )
            } else MatchConditionValueNode(
                type,
                left,
                right,
                location
            )
        }
    }

private fun KSharpParserResult.thenMatchConditionOperator(
    type: MatchConditionalType,
    text: String,
    block: (KSharpLexerIterator) -> KSharpParserResult
): KSharpParserResult =
    resume().thenIf({ it.text == text }) { then ->
        then.consume(block)
    }.buildConditionalMatchValueNode(type)

private fun KSharpLexerIterator.consumeMatchLiteralValue(): KSharpParserResult =
    lookAHead { l ->
        l.ifConsume(KSharpTokenType.OpenBracket, true) { ifLexer ->
            ifLexer.thenLoopIndexed { it, index ->
                if (index > 0) {
                    it.consume(KSharpTokenType.Comma, true)
                        .consume { it.consumeExpressionValue(false) }
                        .build { it.last().cast<NodeData>() }
                } else it.consumeExpressionValue(false)
            }.then(KSharpTokenType.Operator4, "|", false)
                .then(KSharpTokenType.LowerCaseWord)
                .then(KSharpTokenType.CloseBracket, true)
                .build {
                    val tail = it.last().cast<Token>()
                    val head = it.dropLast(2).cast<List<NodeData>>()
                    val location = head.first().location
                    MatchListValueNode(
                        head,
                        LiteralValueNode(tail.text, LiteralValueType.Binding, tail.location),
                        location,
                        MatchListValueNodeLocations(it[it.size - 2].cast<Token>().location)
                    ).cast<NodeData>()
                }
        }.asLookAHeadResult()
    }.or {
        it.consumeExpressionValue()
    }

private fun KSharpLexerIterator.consumeConditionalAndMatchValue(): KSharpParserResult =
    consumeMatchLiteralValue().thenMatchConditionOperator(MatchConditionalType.And, "&&") {
        it.consumeConditionalAndMatchValue()
    }

private fun KSharpLexerIterator.consumeConditionalOrMatchValue(): KSharpParserResult =
    consumeConditionalAndMatchValue()
        .thenMatchConditionOperator(MatchConditionalType.Or, "||") {
            it.consumeConditionalOrMatchValue()
        }

internal fun KSharpLexerIterator.consumeMatchValue(): KSharpParserResult =
    ifConsume(KSharpTokenType.OpenParenthesis, true) { l ->
        l.consume { it.consumeConditionalOrMatchValue() }
            .then(KSharpTokenType.CloseParenthesis, true)
            .build { it.first().cast<NodeData>() }
    }.or { consumeConditionalOrMatchValue() }

internal fun KSharpLexerIterator.consumeMatchExpressionBranch(): KSharpParserResult =
    this.consumeMatchValue()
        .resume()
        .then(KSharpTokenType.Then, false)
        .consume { it.consumeExpression() }
        .discardNewLines()
        .build {
            MatchExpressionBranchNode(
                it.first().cast(),
                it.last().cast(),
                it[1].cast<Token>().location
            )
        }

internal fun KSharpLexerIterator.consumeMatchAssignment(): KSharpParserResult =
    consumeMatchValue()
        .resume()
        .thenAssignOperator()
        .consume { l -> l.consumeExpression() }
        .build {
            val match = it.first().cast<NodeData>()
            MatchAssignNode(
                match,
                it.last().cast(),
                it[1].cast<Token>().location
            )
        }
