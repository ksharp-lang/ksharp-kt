package ksharp.parser

enum class KSharpTokenType : TokenType {
    UpperCaseWord,
    LowerCaseWord,
    FunctionName,
    Integer,
    Float,
    Alt,
    Comma,
    OpenBracket,
    CloseBracket,
    OpenParenthesis,
    CloseParenthesis,
    OpenCurlyBraces,
    CloseCurlyBraces,
    WhiteSpace,
    NewLine,
    Operator,
}

private val mappings = mapOf(
    '@' to KSharpTokenType.Alt,
    ',' to KSharpTokenType.Comma,
    '[' to KSharpTokenType.OpenBracket,
    ']' to KSharpTokenType.CloseBracket,
    '(' to KSharpTokenType.OpenParenthesis,
    ')' to KSharpTokenType.CloseParenthesis,
    '{' to KSharpTokenType.OpenCurlyBraces,
    '}' to KSharpTokenType.CloseCurlyBraces
)

private val operators = "+-*/%><=!&$#^?.\\|".toSet()

fun Char.isNewLine() = this == '\n'
fun Char.isOperator() = operators.contains(this)
fun Char.isDot() = this == '.'

fun Lexer.operator(): LexerToken {
    while (true) {
        val c = this.nextChar() ?: return token(KSharpTokenType.Operator, 1)
        if (!c.isOperator()) {
            return token(KSharpTokenType.Operator, 1)
        }
    }
}

fun Lexer.word(type: TokenType): LexerToken {
    while (true) {
        val c = this.nextChar() ?: return token(type, 1)
        val value = c.isLetter() || c.isDigit() || c == '_'
        if (!value) {
            return token(type, 1)
        }
    }
}

fun Lexer.number(): LexerToken {
    while (true) {
        val c = this.nextChar() ?: return token(KSharpTokenType.Integer, 1)
        if (c.isDot()) return decimal(KSharpTokenType.Integer, 2)
        if (!c.isDigit()) {
            return token(KSharpTokenType.Integer, 1)
        }
    }
}

fun Lexer.decimal(type: TokenType, skip: Int): LexerToken {
    var start = false
    while (true) {
        val c = this.nextChar() ?: return token(type, skip)
        if (!c.isDigit()) {
            if (!start) return token(type, skip)
            return token(KSharpTokenType.Float, 1)
        }
        start = true
    }
}


fun Lexer.newLine(): LexerToken {
    while (true) {
        val c = this.nextChar() ?: return token(KSharpTokenType.NewLine, 1)
        if (c != '\r') {
            return token(KSharpTokenType.NewLine, 1)
        }
    }
}

fun Lexer.whiteSpace(): LexerToken {
    while (true) {
        val c = this.nextChar() ?: return token(KSharpTokenType.WhiteSpace, 1)
        if (!c.isWhitespace()) {
            return token(KSharpTokenType.WhiteSpace, 1)
        }
    }
}


val kSharpTokenFactory: TokenFactory = { c ->
    with(c) {
        when {
            isLetter() ->
                word(
                    if (isUpperCase()) KSharpTokenType.UpperCaseWord
                    else KSharpTokenType.LowerCaseWord
                )

            isDigit() -> number()
            isDot() -> decimal(KSharpTokenType.Operator, 1)
            isNewLine() -> newLine()
            isWhitespace() -> whiteSpace()
            isOperator() -> operator().also { println(it) }
            else -> mappings[c]?.let {
                token(it, 0)
            }
        }
    }
}

private fun canCollapseTokens(current: LexerToken, newToken: LexerToken): Boolean {
    val allowedToken = when (current.type) {
        KSharpTokenType.LowerCaseWord -> true
        KSharpTokenType.UpperCaseWord -> true
        KSharpTokenType.FunctionName -> true
        else -> false
    }
    if (!allowedToken) return false
    return when (newToken.type) {
        KSharpTokenType.Operator -> {
            newToken.text != "."
        }

        KSharpTokenType.LowerCaseWord -> true
        KSharpTokenType.UpperCaseWord -> true
        KSharpTokenType.FunctionName -> true
        KSharpTokenType.Alt -> true
        else -> false
    }
}

fun Iterator<LexerToken>.collapseKSharpTokens(): Iterator<LexerToken> {
    val newTokens = this.collapseTokens()

    return object : Iterator<LexerToken> {
        private var token: LexerToken? = null
        private var lastToken: LexerToken? = null

        override fun hasNext(): Boolean {
            token = lastToken
            lastToken = null
            while (newTokens.hasNext()) {
                lastToken = newTokens.next()
                if (token == null) {
                    token = lastToken
                    continue
                }
                if (canCollapseTokens(token!!, lastToken!!)) {
                    token = token!!.copy(
                        type = KSharpTokenType.FunctionName,
                        token = TextToken(
                            text = "${token!!.text}${lastToken!!.text}",
                            startOffset = token!!.startOffset,
                            endOffset = lastToken!!.endOffset
                        )
                    )
                    continue
                }
                break
            }
            return token != null
        }

        override fun next(): LexerToken = token!!

    }
}
