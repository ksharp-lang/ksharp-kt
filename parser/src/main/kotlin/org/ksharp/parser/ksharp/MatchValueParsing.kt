package org.ksharp.parser.ksharp

import org.ksharp.common.cast
import org.ksharp.nodes.*
import org.ksharp.parser.*

internal fun KSharpLexerIterator.consumeMatchValue(): KSharpParserResult =
    lookAHead { l ->
        l.consume(KSharpTokenType.OpenBracket, true)
            .thenLoopIndexed { it, index ->
                if (index > 0) {
                    it.consume(KSharpTokenType.Comma, true)
                        .consume { it.consumeExpressionValue(false) }
                        .build { it.last().cast<NodeData>() }
                } else it.consumeExpressionValue(false)
            }
            .then(KSharpTokenType.Operator4, "|", false)
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
            .asLookAHeadResult()
    }.or {
        it.consumeExpressionValue()
            .map { value ->
                ParserValue(
                    MatchValueNode(MatchValueType.Expression, value.value, value.value.location),
                    value.remainTokens
                )
            }
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
