package org.ksharp.parser.ksharp

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.common.new
import org.ksharp.nodes.FunctionNode
import org.ksharp.nodes.FunctionNodeLocations
import org.ksharp.nodes.NodeData
import org.ksharp.nodes.UnitNode
import org.ksharp.parser.*

private fun Token.isTypeKeyword(): Boolean =
    type == KSharpTokenType.LowerCaseWord && when (text) {
        "internal" -> false
        "type" -> false
        else -> true
    }

fun KSharpConsumeResult.thenFunctionName(): KSharpConsumeResult =
    then({
        when (it.type) {
            KSharpTokenType.OperatorFunctionName -> true
            KSharpTokenType.FunctionName -> true
            KSharpTokenType.If -> true
            else -> it.isTypeKeyword()
        }
    }, {
        BaseParserErrorCode.ExpectingToken.new(
            "token" to "LowerCaseWord, Operator different internal, type",
            "received-token" to "${it.type}:${it.text}"
        )
    }, false)

fun KSharpLexerIterator.consumeFunctionName(): KSharpConsumeResult =
    consume({
        when (it.type) {
            KSharpTokenType.OperatorFunctionName -> true
            KSharpTokenType.FunctionName -> true
            KSharpTokenType.If -> true
            else -> it.isTypeKeyword()
        }
    }, false)


private fun KSharpConsumeResult.thenFunction(native: Boolean): KSharpParserResult =
    thenIf({
        it.type == KSharpTokenType.LowerCaseWord && it.text == "pub"
    }, false) { it }
        .thenFunctionName()
        .enableDiscardBlockAndNewLineTokens { lx ->
            val funcDecl = lx.thenLoop {
                it.consume(KSharpTokenType.LowerCaseWord)
                    .build { l -> l.first().cast<Token>().text }
            }
            run {
                if (!native) {
                    funcDecl.thenAssignOperator()
                        .consume { it.disableDiscardNewLineToken { l -> l.consumeExpression() } }
                } else funcDecl
            }.build {
                val pub = it.first().cast<Token>().text == "pub"
                val items = if (pub) it.drop(1) else it
                val name = items.first().cast<Token>()
                val expr = if (native) UnitNode(name.location)
                else items.last().cast<NodeData>()
                val arguments = items.filterIsInstance<String>()
                FunctionNode(
                    native,
                    pub,
                    null,
                    name.text,
                    arguments,
                    expr,
                    name.location,
                    FunctionNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            }.map {
                ParserValue(
                    it.value.cast<FunctionNode>()
                        .copy(annotations = it.remainTokens.state.value.annotations.build()),
                    it.remainTokens
                )
            }
        }

fun KSharpLexerIterator.consumeFunction(): KSharpParserResult =
    ifConsume(KSharpTokenType.LowerCaseWord, "native", true) {
        it.thenFunction(true)
    }.or {
        it.collect().thenFunction(false)
    }
