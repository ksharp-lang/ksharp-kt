package org.ksharp.parser

import org.ksharp.common.Location
import org.ksharp.common.Position

interface TokenType

interface Token : LexerValue {
    fun collapse(newType: TokenType, text: String, end: Token): Token
}

interface LexerValue {
    val text: String
    val type: TokenType
}

interface LexerDocumentPosition {
    val startOffset: Int
    val endOffset: Int
}

interface LexerLogicalPosition {
    val context: String
    val startPosition: Position
    val endPosition: Position
}

enum class BaseTokenType : TokenType {
    Unknown
}

data class LexerToken internal constructor(
    override val type: TokenType,
    private val token: TextToken
) : LexerValue, LexerDocumentPosition, Token {
    override val text: String = token.text
    override val startOffset: Int = token.startOffset
    override val endOffset: Int = token.endOffset

    override fun collapse(newType: TokenType, text: String, end: Token): Token {
        end as LexerDocumentPosition
        return copy(
            type = newType,
            token = TextToken(
                text = text,
                startOffset = startOffset,
                endOffset = end.endOffset
            )
        )
    }

}

data class LogicalLexerToken internal constructor(
    val token: LexerToken,
    override val context: String,
    override val startPosition: Position,
    override val endPosition: Position
) : LexerValue by token, LexerDocumentPosition by token, LexerLogicalPosition, Token {

    override fun collapse(newType: TokenType, text: String, end: Token): Token {
        val newToken = token.collapse(newType, text, end)
        end as LexerLogicalPosition
        return copy(
            token = newToken as LexerToken,
            context = context,
            startPosition = startPosition,
            endPosition = end.endPosition
        )
    }
}

val LexerValue.location: Location
    get() =
        if (this is LexerLogicalPosition) {
            val context = context
            val startPosition = startPosition
            Location(context, startPosition)
        } else Location.NoProvided


typealias LexerTokenIterator<V> = LexerIterator<LexerToken, V>
typealias LogicalLexerTokenIterator<V> = LexerIterator<LogicalLexerToken, V>
typealias GenericLexerIterator<V> = LexerIterator<Token, V>