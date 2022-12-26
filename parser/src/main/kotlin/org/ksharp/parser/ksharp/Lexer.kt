package org.ksharp.parser.ksharp

import org.ksharp.common.generateIterator
import org.ksharp.parser.*
import java.io.Reader
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

enum class KSharpTokenType : TokenType {
    UpperCaseWord,
    LowerCaseWord,
    Label,
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

private val operators = "+-*/%><=!&$#^?.\\|:".toSet()

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
            if (type == KSharpTokenType.LowerCaseWord && c == ':') {
                return token(KSharpTokenType.Label, 0)
            }
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

fun LexerValue.isNewLineOrEndExpression() =
    type == KSharpTokenType.NewLine || type == KSharpTokenType.EndExpression

fun LexerValue.isEndExpression() =
    type == KSharpTokenType.EndExpression


@Suppress("UNCHECKED_CAST")
private fun <L : CollapsableToken> Iterator<L>.prepareNewLines(): Iterator<L> {
    var token: L?
    var lastToken: L? = null
    return generateIterator {
        token = lastToken
        lastToken = null
        while (hasNext()) {
            lastToken = next()
            if (token == null) {
                token = lastToken
                lastToken = null
                continue
            }
            if (token!!.type == KSharpTokenType.NewLine) {
                when (lastToken!!.type) {
                    KSharpTokenType.WhiteSpace -> {
                        token = token!!.collapse(
                            KSharpTokenType.NewLine,
                            "\n${lastToken!!.text}",
                            lastToken!!
                        ) as L
                        lastToken = null
                    }

                    else -> {
                        token = token!!.collapse(
                            KSharpTokenType.NewLine,
                            "\n",
                            lastToken!!
                        ) as L?
                    }
                }
            }
            break
        }
        token
    }
}

private fun <L : CollapsableToken> L.whenNewLine(block: (L) -> L?): L? =
    if (type == KSharpTokenType.NewLine) {
        block(this)
    } else this

private fun <L : CollapsableToken> Stack<Int>.processNewLine(
    expressionId: AtomicInteger,
    len: Int,
    token: L,
    expressionToken: (index: Int) -> L
): L? {
    if (isEmpty()) {
        push(len)
        return null
    }
    val lastIndent = peek()
    if (lastIndent == len) {
        return token
    }
    if (lastIndent > len) {
        pop()
        return expressionToken(
            expressionId.incrementAndGet()
        )
    }
    push(len)
    return null
}

fun <L : CollapsableToken> Iterator<L>.markExpressions(
    expressionToken: (index: Int) -> L,
): Iterator<L> {
    val expressionId = AtomicInteger(0)
    val collapseNewLines = prepareNewLines()
    val expressions = Stack<Int>()
    var discardAllExpressions = false
    val withEndExpressions = generateIterator {
        if (discardAllExpressions) {
            expressions.pop()
            discardAllExpressions = expressions.isNotEmpty()
            return@generateIterator expressionToken(
                expressionId.incrementAndGet()
            )
        }
        while (collapseNewLines.hasNext()) {
            val token = collapseNewLines
                .next()
                .whenNewLine {
                    val len = it.text.length
                    if (len == 1) {
                        if (expressions.isNotEmpty()) {
                            expressions.pop()
                            discardAllExpressions = expressions.isNotEmpty()
                        }
                        return@whenNewLine expressionToken(
                            expressionId.incrementAndGet()
                        )
                    }
                    expressions.processNewLine(expressionId, len, it, expressionToken)
                } ?: continue
            return@generateIterator token
        }
        if (expressions.isEmpty()) return@generateIterator null
        expressions.pop()
        return@generateIterator expressionToken(
            expressionId.incrementAndGet()
        )
    }.collapseNewLines()
    return withEndExpressions
}

@Suppress("UNCHECKED_CAST")
fun <L : CollapsableToken> Iterator<L>.collapseNewLines(): Iterator<L> = collapseTokens(
    predicate = { start, end ->
        when {
            start.isEndExpression() && end.isEndExpression() -> false
            else -> start.isNewLineOrEndExpression() && end.isNewLineOrEndExpression()
        }
    }
) { start, end ->
    start.collapse(
        if (start.isEndExpression() || end.isEndExpression())
            KSharpTokenType.EndExpression
        else KSharpTokenType.NewLine,
        "",
        end
    ) as L
}

private fun Iterator<LexerToken>.ensureNewLineAtEnd(): Iterator<LexerToken> {
    var lastToken: LexerToken? = null
    return generateIterator {
        if (hasNext()) {
            val result = next()
            lastToken = result
            return@generateIterator result
        }
        if (lastToken != null && lastToken!!.type != KSharpTokenType.NewLine) {
            val offset = lastToken!!.endOffset + 1
            val result = LexerToken(
                KSharpTokenType.NewLine,
                token = TextToken("\n", offset, offset)
            )
            lastToken = null
            return@generateIterator result
        }
        null
    }
}

fun Iterator<LexerToken>.collapseKSharpTokens(): Iterator<LexerToken> {
    val newTokens = this.ensureNewLineAtEnd()
        .collapseTokens()

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
    }
}

fun String.kSharpLexer() = lexer(charStream(), kSharpTokenFactory)
fun Reader.kSharpLexer() = lexer(charStream(), kSharpTokenFactory)