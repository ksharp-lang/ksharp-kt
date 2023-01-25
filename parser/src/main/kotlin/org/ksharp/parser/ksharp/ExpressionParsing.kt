package org.ksharp.parser.ksharp

import org.ksharp.common.cast
import org.ksharp.nodes.LiteralCollectionNode
import org.ksharp.nodes.LiteralCollectionType
import org.ksharp.nodes.NodeData
import org.ksharp.parser.*

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
        it.consumeExpressionValue(false)
    }
        .build {
            println(it)
            TODO()
        }

fun KSharpLexerIterator.consumeExpressionValue(tupleWithoutParenthesis: Boolean = true): KSharpParserResult {
    val literal = ifConsume(KSharpTokenType.OpenParenthesis, true) {
        it.consume { l -> l.consumeExpressionValue() }
            .then(KSharpTokenType.CloseParenthesis, true)
            .build { l ->
                l.first().cast<NodeData>()
            }
    }.or { it.consumeLiteral() }
        .or { it.consumeFunctionCall() }
    return if (tupleWithoutParenthesis) {
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
}