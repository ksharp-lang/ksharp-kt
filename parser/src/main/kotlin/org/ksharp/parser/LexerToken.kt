package org.ksharp.parser

import org.ksharp.common.Location
import org.ksharp.common.Position

interface TokenType

interface Token : LexerValue, LexerDocumentPosition {
    fun collapse(newType: TokenType, text: String, end: Token): Token

    fun new(type: TokenType): Token
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
    val startPosition: Position
    val endPosition: Position
}

enum class BaseTokenType : TokenType {
    Unknown
}

data class LexerToken internal constructor(
    override val type: TokenType,
    private val token: TextToken
) : Token {
    override val text: String = token.text
    override val startOffset: Int = token.startOffset
    override val endOffset: Int = token.endOffset

    override fun collapse(newType: TokenType, text: String, end: Token): Token {
        return copy(
            type = newType,
            token = TextToken(
                text = text,
                startOffset = startOffset,
                endOffset = end.endOffset
            )
        )
    }

    override fun new(type: TokenType): Token = this.copy(type = type)
}

data class LogicalLexerToken internal constructor(
    val token: LexerToken,
    override val startPosition: Position,
    override val endPosition: Position
) : LexerValue by token, LexerDocumentPosition by token, LexerLogicalPosition, Token {

    override fun collapse(newType: TokenType, text: String, end: Token): Token {
        val newToken = token.collapse(newType, text, end)
        end as LexerLogicalPosition
        return copy(
            token = newToken as LexerToken,
            startPosition = startPosition,
            endPosition = end.endPosition
        )
    }

    override fun new(type: TokenType): Token = this.copy(token = token.copy(type = type))

}

val LexerValue.location: Location
    get() =
        if (this is LexerLogicalPosition) {
            Location(startPosition, endPosition)
        } else Location.NoProvided
