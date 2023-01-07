package org.ksharp.parser.ksharp

import org.ksharp.parser.*
import java.io.Reader

data class KSharpLexerState(
    val consumeLabels: Boolean = false,
    val discardBlockTokens: Boolean = false,
    val discardNewLineToken: Boolean = false
)

typealias KSharpLexer = Lexer<KSharpLexerState>
typealias KSharpLexerIterator = BaseLexerIterator<KSharpLexerState>

enum class KSharpTokenType : TokenType {
    Ignore,
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

    BeginBlock,
    EndBlock
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

fun Char.isSpace() = this == ' ' || this == '\t'

fun Char.isOperator() = operators.contains(this)
fun Char.isDot() = this == '.'

fun Char.shouldIgnore() = this == '\r'

fun <R> KSharpLexerIterator.enableLabelToken(code: (KSharpLexerIterator) -> R): R =
    state.value.consumeLabels.let { initValue ->
        state.update(state.value.copy(consumeLabels = true))
        code(this).also {
            state.update(state.value.copy(consumeLabels = initValue))
        }
    }

fun <R> KSharpLexerIterator.enableDiscardBlocksTokens(code: (KSharpLexerIterator) -> R): R =
    state.value.discardBlockTokens.let { initValue ->
        state.update(state.value.copy(discardBlockTokens = true))
        code(this).also {
            state.update(state.value.copy(discardBlockTokens = initValue))
        }
    }

fun <R> KSharpLexerIterator.enableDiscardNewLineToken(code: (KSharpLexerIterator) -> R): R =
    state.value.discardNewLineToken.let { initValue ->
        state.update(state.value.copy(discardNewLineToken = true))
        code(this).also {
            state.update(state.value.copy(discardNewLineToken = initValue))
        }
    }

fun <R> KSharpLexerIterator.enableDiscardBlockAndNewLineTokens(code: (KSharpLexerIterator) -> R): R =
    enableDiscardBlocksTokens {
        enableDiscardNewLineToken(code)
    }

fun KSharpLexer.operator(): LexerToken {
    while (true) {
        val c = this.nextChar() ?: return token(KSharpTokenType.Operator, 1)
        if (!c.isOperator()) {
            return token(KSharpTokenType.Operator, 1)
        }
    }
}

fun KSharpLexer.word(type: TokenType): LexerToken {
    while (true) {
        val c = this.nextChar() ?: return token(type, 1)
        val value = c.isLetter() || c.isDigit() || c == '_'
        if (!value) {
            if (state.value.consumeLabels && type == KSharpTokenType.LowerCaseWord && c == ':') {
                return token(KSharpTokenType.Label, 0)
            }
            return token(type, 1)
        }
    }
}

fun KSharpLexer.number(): LexerToken {
    while (true) {
        val c = this.nextChar() ?: return token(KSharpTokenType.Integer, 1)
        if (c.isDot()) return decimal(KSharpTokenType.Integer, 2)
        if (!c.isDigit()) {
            return token(KSharpTokenType.Integer, 1)
        }
    }
}

fun KSharpLexer.decimal(type: TokenType, skip: Int): LexerToken {
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


fun KSharpLexer.newLine(): LexerToken {
    while (true) {
        val c = this.nextChar() ?: return token(KSharpTokenType.NewLine, 1)
        if (!c.isSpace()) {
            return token(KSharpTokenType.NewLine, 1)
        }
    }
}

fun KSharpLexer.whiteSpace(): LexerToken {
    while (true) {
        val c = this.nextChar() ?: return token(KSharpTokenType.WhiteSpace, 1)
        if (!c.isSpace()) {
            return token(KSharpTokenType.WhiteSpace, 1)
        }
    }
}


val kSharpTokenFactory: TokenFactory<KSharpLexerState> = { c ->
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
            isSpace() -> whiteSpace()
            isOperator() -> operator()
            shouldIgnore() -> token(KSharpTokenType.Ignore, 0)
            else -> mappings[c]?.let {
                token(it, 0)
            }
        }
    }
}

private fun canCollapseTokens(current: Token, newToken: Token): Boolean {
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
private fun Token.mapOperatorToken(): Token = when (type) {
    KSharpTokenType.Operator -> {
        when {
            text == "**" -> new(type = KSharpTokenType.Operator1)
            text == "<<" || text == ">>" -> new(type = KSharpTokenType.Operator4)
            text == "&&" -> new(type = KSharpTokenType.Operator10)
            text == "||" -> new(type = KSharpTokenType.Operator11)
            text == "=" -> new(type = KSharpTokenType.Operator12)

            text.isEmpty() -> this

            operator2.contains(text.first()) -> new(type = KSharpTokenType.Operator2)
            operator3.contains(text.first()) -> new(type = KSharpTokenType.Operator3)
            operator5.contains(text.first()) -> new(type = KSharpTokenType.Operator5)
            operator6.contains(text.first()) -> new(type = KSharpTokenType.Operator6)
            operator7.contains(text.first()) -> new(type = KSharpTokenType.Operator7)
            operator8.contains(text.first()) -> new(type = KSharpTokenType.Operator8)
            operator9.contains(text.first()) -> new(type = KSharpTokenType.Operator9)
            else -> this
        }
    }

    else -> this
}

internal fun String.indentLength() =
    replace("\n", "") //normalize newline to zero spaces
        .replace("\t", "  ") //normalize tab to two spaces
        .length + 1 // add one that represent the newline

private fun KSharpLexerIterator.collapseNewLines(): KSharpLexerIterator {
    var lastIndent = 0
    return generateLexerIterator(state) {
        while (hasNext()) {
            val token = next()
            lastIndent = if (token.type == KSharpTokenType.NewLine) {
                val length = token.text.indentLength()
                if (length == lastIndent) continue
                else length
            } else 0
            return@generateLexerIterator token
        }
        null
    }
}

private fun KSharpLexerIterator.discardBlocksOrNewLineTokens(): KSharpLexerIterator {
    var beginBlock = 0
    return generateLexerIterator(state) {
        while (hasNext()) {
            val item = next()
            val discardBlockTokens = state.value.discardBlockTokens
            if (discardBlockTokens) {
                if (item.type == KSharpTokenType.BeginBlock) {
                    beginBlock += 1
                    continue
                }
                if (item.type == KSharpTokenType.EndBlock) {
                    if (beginBlock == 0) {
                        return@generateLexerIterator item
                    }
                    beginBlock -= 1
                    continue
                }
            }
            if (item.type == KSharpTokenType.NewLine && state.value.discardNewLineToken) {
                continue
            }
            return@generateLexerIterator item
        }
        null
    }
}

fun KSharpLexerIterator.markBlocks(
    expressionToken: (TokenType) -> Token,
): KSharpLexerIterator {
    val collapseNewLines = this.collapseNewLines()
    val controller = BlockController(expressionToken)
    val withEndExpressions = generateLexerIterator(state) {
        val pendingToken = controller.pendingToken()
        if (pendingToken != null) {
            return@generateLexerIterator pendingToken
        }
        if (collapseNewLines.hasNext()) {
            return@generateLexerIterator controller.processToken(collapseNewLines.next())
        }
        controller.end()
    }.discardBlocksOrNewLineTokens()
    return withEndExpressions
}

private fun KSharpLexerIterator.ensureNewLineAtEnd(): KSharpLexerIterator {
    var lastToken: Token? = null
    return generateLexerIterator(state) {
        if (hasNext()) {
            val result = next()
            lastToken = result
            return@generateLexerIterator result
        }
        if (lastToken != null && lastToken!!.type != KSharpTokenType.NewLine) {
            val offset = lastToken!!.endOffset + 1
            val result = LexerToken(
                KSharpTokenType.NewLine,
                token = TextToken("\n", offset, offset)
            )
            lastToken = null
            return@generateLexerIterator result
        }
        null
    }
}

fun KSharpLexerIterator.collapseKSharpTokens(): KSharpLexerIterator {
    val newTokens = this.ensureNewLineAtEnd()
        .collapseTokens {
            it != KSharpTokenType.NewLine
        }

    var token: Token?
    var lastToken: Token? = null
    var lastWasNewLine = false

    return generateLexerIterator(state) {
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
                token = token!!.collapse(
                    KSharpTokenType.FunctionName,
                    "${token!!.text}${lastToken!!.text}",
                    lastToken!!
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

fun String.kSharpLexer() = lexer(KSharpLexerState(), charStream(), kSharpTokenFactory).filter {
    it.type != KSharpTokenType.Ignore
}

fun Reader.kSharpLexer() = lexer(KSharpLexerState(), charStream(), kSharpTokenFactory).filter {
    it.type != KSharpTokenType.Ignore
}