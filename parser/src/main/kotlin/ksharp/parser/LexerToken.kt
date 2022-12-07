package ksharp.parser

interface TokenType

enum class BaseTokenType : TokenType {
    Unknown
}

data class LexerToken internal constructor(
    val type: TokenType,
    private val token: TextToken
) {
    val text: String = token.text
    val startOffset: Int = token.startOffset
    val endOffset: Int = token.endOffset
}
