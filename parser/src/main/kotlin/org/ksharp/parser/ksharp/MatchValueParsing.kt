package org.ksharp.parser.ksharp

import org.ksharp.common.*
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
                    MatchValueNode(
                        MatchValueType.List,
                        MatchListValueNode(
                            head,
                            LiteralValueNode(tail.text, LiteralValueType.Binding, tail.location),
                            location,
                            MatchListValueNodeLocations(it[it.size - 2].cast<Token>().location)
                        ),
                        location
                    ).cast<NodeData>()
                }
        }.asLookAHeadResult()
    }.or {
        it.consumeExpressionValue()
            .map { value ->
                ParserValue(
                    MatchValueNode(MatchValueType.Expression, value.value, value.value.location),
                    value.remainTokens
                )
            }
    }

private fun KSharpLexerIterator.consumeConditionalMatchValue(): KSharpParserResult =
    this.collect()
        .thenLoopIndexed { lexer, index ->
            if (index != 0) {
                lexer.consume({ it.text == "&&" || it.text == "||" }, false)
                    .consume { it.consumeMatchValue() }
                    .build {
                        val token = it.first().cast<Token>()
                        MatchValueNode(
                            if (token.type == KSharpTokenType.Operator3) MatchValueType.And else MatchValueType.Or,
                            it.last().cast(),
                            token.location
                        )
                    }
            } else lexer.consumeMatchLiteralValue()
        }.build {
            if (it.isEmpty()) NoMatch
            else {
                val first = it.first().cast<NodeData>()
                if (it.size == 1) first
                else MatchValueNode(
                    MatchValueType.Group,
                    GroupMatchValueNode(
                        it.cast(),
                        first.location
                    ),
                    first.location
                )
            }
        }.flatMap {
            if (it.value == NoMatch) Either.Left(
                ParserError(
                    MatchParsingErrorCode.NoMatchFound.new(Location.NoProvided),
                    listBuilder(),
                    false,
                    it.remainTokens
                )
            )
            else Either.Right(it)
        }

internal fun KSharpLexerIterator.consumeMatchValue(): KSharpParserResult =
    ifConsume(KSharpTokenType.OpenParenthesis, true) { l ->
        l.consume { it.consumeConditionalMatchValue() }
            .then(KSharpTokenType.CloseParenthesis, true)
            .build { it.first().cast<NodeData>() }
    }.or { consumeConditionalMatchValue() }

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
            val match = it.first().cast<MatchValueNode>()
            MatchAssignNode(
                match,
                it.last().cast(),
                match.location,
                MatchAssignNodeLocations(it[1].cast<Token>().location)
            )
        }
