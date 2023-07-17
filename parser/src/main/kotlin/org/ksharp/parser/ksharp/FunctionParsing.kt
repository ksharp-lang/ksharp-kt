package org.ksharp.parser.ksharp

import org.ksharp.common.Location
import org.ksharp.common.add
import org.ksharp.common.cast
import org.ksharp.common.listBuilder
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
        createExpectedTokenError("LowerCaseWord, Operator different internal, type", it)
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

private fun createFunctionNode(native: Boolean, emitLocations: Boolean, it: List<Any>): FunctionNode {
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
    return FunctionNode(
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
}

private fun KSharpConsumeResult.thenFunction(native: Boolean, emitLocations: Boolean): KSharpParserResult =
    thenIf({
        it.type == KSharpTokenType.LowerCaseWord && it.text == "pub"
    }, false) { it }
        .thenFunctionName()
        .thenWithIndentationOffset(IndentationOffsetType.Relative, OffsetType.Normal) { lx ->
            val funcDecl = lx.thenLoop {
                it.consume(KSharpTokenType.LowerCaseWord)
                    .build { l -> l.first() }
            }
            run {
                if (!native) {
                    funcDecl.thenAssignOperator()
                        .consume { l -> l.consumeExpression() }
                } else funcDecl
            }
        }.build {
            createFunctionNode(native, emitLocations, it)
        }
        .map {
            ParserValue(
                it.value.cast<FunctionNode>()
                    .copy(annotations = it.remainTokens.state.value.annotations.build()),
                it.remainTokens
            )
        }

fun KSharpLexerIterator.consumeFunction(): KSharpParserResult =
    ifConsume(KSharpTokenType.LowerCaseWord, "native", false) {
        it.thenFunction(true, state.value.emitLocations)
    }.or {
        it.collect().thenFunction(false, state.value.emitLocations)
    }
