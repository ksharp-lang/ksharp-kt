package org.ksharp.parser.ksharp

import org.ksharp.common.cast
import org.ksharp.nodes.LiteralCollectionNode
import org.ksharp.nodes.LiteralCollectionType
import org.ksharp.nodes.NodeData
import org.ksharp.parser.*

fun KSharpLexerIterator.consumeExpression(tupleWithoutParenthesis: Boolean = true): KSharpParserResult {
    val literal = ifConsume(KSharpTokenType.OpenParenthesis, true) {
        it.consume { l -> l.consumeExpression() }
            .then(KSharpTokenType.CloseParenthesis, true)
            .build { l ->
                l.first().cast<NodeData>()
            }
    }.or { it.consumeLiteral() }
    return if (tupleWithoutParenthesis) {
        literal
            .resume()
            .thenLoop {
                it.consume(KSharpTokenType.Comma, true)
                    .consume { l -> l.consumeExpression(false) }
                    .build { l -> l.first().cast() }
            }.build {
                if (it.size == 1) it.first().cast<NodeData>()
                else LiteralCollectionNode(it.cast(), LiteralCollectionType.Tuple, it.first().cast<NodeData>().location)
            }
    } else literal
}