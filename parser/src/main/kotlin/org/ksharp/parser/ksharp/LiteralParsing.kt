package org.ksharp.parser.ksharp

import org.ksharp.common.cast
import org.ksharp.nodes.*
import org.ksharp.parser.*

private fun KSharpConsumeResult.buildLiteralValue(type: LiteralValueType): KSharpParserResult =
    build {
        val token = it.first().cast<Token>()
        LiteralValueNode(token.text, type, token.location)
    }

private fun KSharpLexerIterator.consumeLiteralValue(token: KSharpTokenType, type: LiteralValueType) =
    consume(token).buildLiteralValue(type)

private fun KSharpParserResult.orConsumeLiteralValue(token: KSharpTokenType, type: LiteralValueType) =
    or { it.consumeLiteralValue(token, type) }

private fun KSharpLexerIterator.consumeListOrSetLiteral(): KSharpParserResult =
    consume({
        it.type == KSharpTokenType.OpenBracket ||
                it.type == KSharpTokenType.OpenSetBracket
    }).thenLoopIndexed { lexer, index ->
        if (index > 0) {
            lexer.consume(KSharpTokenType.Comma, true)
                .consume { it.consumeExpression(false) }
                .build { it.last().cast() }
        } else lexer.consumeExpression(false)
    }.then(KSharpTokenType.CloseBracket, true)
        .build {
            val token = it.first().cast<Token>()
            val type = when (token.type) {
                KSharpTokenType.OpenBracket -> LiteralCollectionType.List
                KSharpTokenType.OpenSetBracket -> LiteralCollectionType.Set
                else -> TODO("")
            }
            val location = token.location
            LiteralCollectionNode(it.drop(1).cast(), type, location)
        }

private fun KSharpLexerIterator.consumeMapEntryLiteral(): KSharpParserResult =
    consumeExpression(false)
        .resume()
        .then(KSharpTokenType.Operator, ":", true)
        .consume { it.consumeExpression(false) }
        .build {
            val first = it.first().cast<NodeData>()
            LiteralMapEntryNode(first, it.last().cast(), first.location)
        }

private fun KSharpLexerIterator.consumeMapLiteral(): KSharpParserResult =
    consume(KSharpTokenType.OpenCurlyBraces)
        .thenLoopIndexed { lexer, index ->
            if (index > 0) {
                lexer.consume(KSharpTokenType.Comma, true)
                    .consume { it.consumeMapEntryLiteral() }
                    .build { it.last().cast() }
            } else lexer.consumeMapEntryLiteral()
        }.then(KSharpTokenType.CloseCurlyBraces, true)
        .build {
            val token = it.first().cast<Token>()
            val type = LiteralCollectionType.Map
            val location = token.location
            LiteralCollectionNode(it.drop(1).cast(), type, location)
        }

fun KSharpLexerIterator.consumeLiteral() =
    consumeLiteralValue(KSharpTokenType.Character, LiteralValueType.Character)
        .orConsumeLiteralValue(KSharpTokenType.String, LiteralValueType.String)
        .orConsumeLiteralValue(KSharpTokenType.MultiLineString, LiteralValueType.MultiLineString)
        .orConsumeLiteralValue(KSharpTokenType.Integer, LiteralValueType.Integer)
        .orConsumeLiteralValue(KSharpTokenType.HexInteger, LiteralValueType.HexInteger)
        .orConsumeLiteralValue(KSharpTokenType.OctalInteger, LiteralValueType.OctalInteger)
        .orConsumeLiteralValue(KSharpTokenType.BinaryInteger, LiteralValueType.BinaryInteger)
        .orConsumeLiteralValue(KSharpTokenType.Float, LiteralValueType.Decimal)
        .or { it.consumeListOrSetLiteral() }
        .or { it.consumeMapLiteral() }


