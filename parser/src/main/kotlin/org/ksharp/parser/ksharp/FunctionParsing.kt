package org.ksharp.parser.ksharp

import org.ksharp.common.*
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


private fun KSharpConsumeResult.thenFunction(native: Boolean, emitLocations: Boolean): KSharpParserResult =
    thenIf({
        it.type == KSharpTokenType.LowerCaseWord && it.text == "pub"
    }, false) { it }
        .thenFunctionName()
        .enableDiscardBlockAndNewLineTokens { lx ->
            val funcDecl = lx.thenLoop {
                it.consume(KSharpTokenType.LowerCaseWord)
                    .build { l -> l.first() }
            }
            run {
                if (!native) {
                    funcDecl.thenAssignOperator()
                        .consume { it.disableDiscardNewLineToken { l -> l.consumeExpression() } }
                } else funcDecl
            }.build {
                var index = 0
                val nativeLocation = if (native) it[index++].cast<Token>().location else Location.NoProvided
                val pub = it[index++].cast<Token>()
                val pubLocation = if (pub.text == "pub") pub.location else Location.NoProvided
                val name = if (pub.text == "pub") it[index++].cast() else pub
                val expr = if (native) UnitNode(name.location) else it.last().cast<NodeData>()
                val argumentsLocation = listBuilder<Location>()
                val arguments = it.subList(index, if (native) it.size else it.size - 2)
                    .map {
                        val t = it.cast<Token>()
                        if (emitLocations) argumentsLocation.add(t.location)
                        t.text
                    }
                FunctionNode(
                    native,
                    pub.text == "pub",
                    null,
                    name.text,
                    arguments,
                    expr,
                    name.location,
                    FunctionNodeLocations(
                        nativeLocation,
                        pubLocation,
                        name.location,
                        argumentsLocation.build(),
                        if (native) Location.NoProvided else it[it.size - 2].cast<Token>().location
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
    ifConsume(KSharpTokenType.LowerCaseWord, "native", false) {
        it.thenFunction(true, state.value.emitLocations)
    }.or {
        it.collect().thenFunction(false, state.value.emitLocations)
    }
