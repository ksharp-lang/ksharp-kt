package org.ksharp.parser.ksharp

import org.ksharp.common.ErrorCode
import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.nodes.*
import org.ksharp.parser.*

private object NoMatch : NodeData() {
    override val locations: NodeLocations = NoLocationsDefined
    override val location: Location = Location.NoProvided
    override val children: Sequence<NodeData> = emptySequence()
}

private enum class MatchParsingErrorCode(override val description: String) : ErrorCode {
    NoMatchFound("No match found"),
}

private fun KSharpConsumeResult.buildConditionalMatchValueNode(type: MatchConditionalType): KSharpParserResult =
    build {
        if (it.size == 1) it.first().cast<NodeData>()
        else {
            MatchConditionValueNode(
                type,
                it.first().cast(),
                it.last().cast(),
                it[1].cast<Token>().location
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

private fun KSharpLexerIterator.consumeConditionalOrMatchValue(): KSharpParserResult =
    consumeMatchValue().thenMatchConditionOperator(MatchConditionalType.And, "||") {
        it.consumeConditionalOrMatchValue()
    }

private fun KSharpLexerIterator.consumeConditionalAndMatchValue(): KSharpParserResult =
    consumeConditionalOrMatchValue()
        .thenMatchConditionOperator(MatchConditionalType.And, "&&") {
            it.consumeConditionalAndMatchValue()
        }

internal fun KSharpLexerIterator.consumeMatchValue(): KSharpParserResult =
    ifConsume(KSharpTokenType.OpenParenthesis, true) { l ->
        l.consume { it.consumeConditionalAndMatchValue() }
            .then(KSharpTokenType.CloseParenthesis, true)
            .build { it.first().cast<NodeData>() }
    }.or { consumeConditionalAndMatchValue() }

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

internal fun KSharpLexerIterator.consumeMatchAssignment() =
    consumeMatchValue()
        .resume()
        .thenAssignOperator()
        .disableExpressionStartingNewLine {
            it.consume { l -> l.consumeExpression() }
        }
        .build {
            val match = it.first().cast<NodeData>()
            MatchAssignNode(
                match,
                it.last().cast(),
                it[1].cast<Token>().location
            )
        }
