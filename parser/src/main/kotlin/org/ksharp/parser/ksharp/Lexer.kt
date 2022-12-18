package org.ksharp.parser.ksharp

import org.ksharp.common.generateIterator
import org.ksharp.parser.*
import java.io.Reader
import java.util.*

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

    Operator1,
    Operator2,
    Operator3,
    Operator4,
    Operator5,
    Operator6,
    Operator7,
    Operator8,
    Operator9,
    Operator10,
    Operator11,
    Operator12,

    EndExpression
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
        if (!(c.isWhitespace() && !c.isNewLine())) {
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
            isOperator() -> operator()
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

private val operator2 = "*/%".asSequence().toSet()
private val operator3 = "+-".asSequence().toSet()
private val operator5 = "<>".asSequence().toSet()
private val operator6 = "=!".asSequence().toSet()
private val operator7 = "&".asSequence().toSet()
private val operator8 = "^".asSequence().toSet()
private val operator9 = "|".asSequence().toSet()

/// https://docs.ksharp.org/rfc/syntax#operator-precedence
private fun LexerToken.mapOperatorToken(): LexerToken = when (type) {
    KSharpTokenType.Operator -> {
        when {
            text == "**" -> copy(type = KSharpTokenType.Operator1)
            text == "<<" || text == ">>" -> copy(type = KSharpTokenType.Operator4)
            text == "&&" -> copy(type = KSharpTokenType.Operator10)
            text == "||" -> copy(type = KSharpTokenType.Operator11)
            text == "=" -> copy(type = KSharpTokenType.Operator12)

            text.isEmpty() -> this

            operator2.contains(text.first()) -> copy(type = KSharpTokenType.Operator2)
            operator3.contains(text.first()) -> copy(type = KSharpTokenType.Operator3)
            operator5.contains(text.first()) -> copy(type = KSharpTokenType.Operator5)
            operator6.contains(text.first()) -> copy(type = KSharpTokenType.Operator6)
            operator7.contains(text.first()) -> copy(type = KSharpTokenType.Operator7)
            operator8.contains(text.first()) -> copy(type = KSharpTokenType.Operator8)
            operator9.contains(text.first()) -> copy(type = KSharpTokenType.Operator9)
            else -> this
        }
    }

    else -> this
}

private fun <L : LexerValue> Iterator<L>.removeEmptyExpressions(): Iterator<L> {
    var token: L? = null
    return generateIterator {
        if (token == null) {
            if (this@removeEmptyExpressions.hasNext()) {
                token = this@removeEmptyExpressions.next()
            } else return@generateIterator null
        }

        if (token!!.type != KSharpTokenType.EndExpression) {
            val result = token
            token = null
            return@generateIterator result
        }

        while (this@removeEmptyExpressions.hasNext()) {
            val next = this@removeEmptyExpressions.next()
            if (next.type != KSharpTokenType.EndExpression) {
                val result = token
                token = next
                return@generateIterator result
            }
        }

        val result = token
        token = null
        result
    }
}

fun <L : LexerValue> Iterator<L>.markExpressions(
    endExpressionToken: L
): Iterator<L> {
    val expressions = Stack<Int>()
    var isLast = false
    val withEndExpressions = generateIterator {
        if (isLast) return@generateIterator null
        while (this@markExpressions.hasNext()) {
            val token = this@markExpressions.next()
            if (token.type == KSharpTokenType.NewLine) {
                if (token.text.length == 1) {
                    if (expressions.isNotEmpty()) {
                        expressions.pop()
                    }
                    return@generateIterator endExpressionToken
                }
                val len = token.text.length
                if (expressions.isEmpty()) {
                    expressions.push(len)
                    continue
                }
                val lastIndent = expressions.peek()
                if (lastIndent == len) {
                    continue
                }
                if (lastIndent > len) {
                    expressions.pop()
                    return@generateIterator endExpressionToken
                }
            }
            return@generateIterator token
        }
        isLast = true
        endExpressionToken
    }
    return withEndExpressions.removeEmptyExpressions()
}

private fun Iterator<LexerToken>.collapseNewLines(): Iterator<LexerToken> {
    var token: LexerToken?
    var lastToken: LexerToken? = null
    return generateIterator {
        token = lastToken
        lastToken = null
        while (this@collapseNewLines.hasNext()) {
            lastToken = this@collapseNewLines.next()
            if (token == null) {
                token = lastToken
                lastToken = null
                continue
            }
            if (token!!.type == KSharpTokenType.NewLine
                && lastToken!!.type == KSharpTokenType.WhiteSpace
            ) {
                token = token!!.copy(
                    type = KSharpTokenType.NewLine,
                    token = TextToken(
                        text = "\n${lastToken!!.text}",
                        startOffset = token!!.startOffset,
                        endOffset = lastToken!!.endOffset
                    )
                )
                lastToken = null
            }
            break
        }
        token
    }
}

fun Iterator<LexerToken>.collapseKSharpTokens(): Iterator<LexerToken> {
    val newTokens = this.collapseTokens()

    var token: LexerToken?
    var lastToken: LexerToken? = null
    var lastWasNewLine = false

    return generateIterator {
        token = lastToken
        lastToken = null
        while (newTokens.hasNext()) {
            lastToken = newTokens.next()
            if (token == null) {
                token = lastToken
                lastToken = null
                continue
            }

            if (token!!.type == KSharpTokenType.WhiteSpace) {
                if (lastWasNewLine) {
                    break
                }
                token = lastToken
                lastToken = null
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
                lastToken = null
                continue
            }

            lastWasNewLine = false
            break
        }

        lastWasNewLine = token?.type == KSharpTokenType.NewLine
        token?.mapOperatorToken()
    }.collapseNewLines()
}

fun String.kSharpLexer() = lexer(charStream(), kSharpTokenFactory)
fun Reader.kSharpLexer() = lexer(charStream(), kSharpTokenFactory)