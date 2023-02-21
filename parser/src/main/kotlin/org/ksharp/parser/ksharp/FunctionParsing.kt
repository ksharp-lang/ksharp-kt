package org.ksharp.parser.ksharp

import org.ksharp.common.cast
import org.ksharp.common.new
import org.ksharp.nodes.FunctionNode
import org.ksharp.nodes.NodeData
import org.ksharp.parser.*

private fun Token.isTypeKeyword(): Boolean =
    type == KSharpTokenType.LowerCaseWord && when (text) {
        "internal" -> false
        "type" -> false
        else -> true
    }

private fun KSharpConsumeResult.thenFunction(pub: Boolean): KSharpParserResult =
    then({ it.isTypeKeyword() }, {
        BaseParserErrorCode.ExpectingToken.new(
            "token" to "LowerCaseWord different internal, type",
            "received-token" to "${it.type}:${it.text}"
        )
    }, false)
        .enableDiscardBlockAndNewLineTokens { lx ->
            lx.thenLoop {
                it.consume(KSharpTokenType.LowerCaseWord)
                    .build { l -> l.first().cast<Token>().text }
            }.thenAssignOperator()
                .consume { it.disableDiscardNewLineToken { l -> l.consumeExpression() } }
                .build {
                    val name = it.first().cast<Token>()
                    val expr = it.last().cast<NodeData>()
                    val arguments = it.filterIsInstance<String>()
                    FunctionNode(
                        pub,
                        name.text,
                        arguments,
                        expr,
                        name.location
                    ) as NodeData
                }
        }


fun KSharpLexerIterator.consumeFunction(): KSharpParserResult =
    ifConsume(KSharpTokenType.LowerCaseWord, "pub", true) {
        it.thenFunction(true)
    }.or {
        it.collect().thenFunction(false)
    }