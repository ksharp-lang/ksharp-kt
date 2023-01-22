package org.ksharp.parser.ksharp

import org.ksharp.parser.*
import java.io.Reader
import java.util.concurrent.atomic.AtomicInteger

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
    String,
    MultiLineString,
    Character,
    HexInteger,
    BinaryInteger,
    OctalInteger,
    Integer,
    Float,
    Alt,
    Comma,
    OpenBracket,
    CloseBracket,
    OpenParenthesis,
    CloseParenthesis,
    OpenSetBracket,
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
private val hexLetterDigit = "0123456789abcdefABCDEF".toSet()
private val octalLetterDigit = "01234567".toSet()
private val binaryDigit = "01".toSet()
private val escapeCharacters = "t'\"rnf\\b".toSet()

fun Char.isEscapeCharacter() = escapeCharacters.contains(this)

fun Char.isNewLine() = this == '\n'

fun Char.isSpace() = this == ' ' || this == '\t'

fun Char.isOperator() = operators.contains(this)
fun Char.isDot() = this == '.'

fun Char.shouldIgnore() = this == '\r'
private inline fun KSharpLexer.ifChar(
    predicate: Char.() -> Boolean,
    eofToken: TokenType,
    then: KSharpLexer.(Char) -> LexerToken,
    elseThen: KSharpLexer.(Char) -> LexerToken,
): LexerToken {
    val c = nextChar() ?: return token(eofToken, 1)
    if (c.predicate()) return then(c)
    return elseThen(c)
}

private inline fun KSharpLexer.ifChar(
    expected: Char,
    eofToken: TokenType,
    thenToken: TokenType,
    elseThen: KSharpLexer.(Char) -> LexerToken = { token(eofToken, 1) }
): LexerToken = ifChar({ equals(expected) }, eofToken, {
    token(thenToken, 0)
}, elseThen)

private inline fun KSharpLexer.loopChar(
    predicate: Char.() -> Boolean,
    endToken: TokenType,
    elseToken: KSharpLexer.(Char) -> LexerToken
): LexerToken {
    while (true) {
        val c = this.nextChar() ?: return token(endToken, 1)
        if (!c.predicate()) {
            return elseToken(c)
        }
    }
}

private inline fun KSharpLexer.loopChar(
    predicate: Char.() -> Boolean,
    endToken: TokenType
): LexerToken = loopChar(predicate, endToken) { token(endToken, 1) }

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

fun KSharpLexer.operator(): LexerToken = loopChar({ isOperator() }, KSharpTokenType.Operator)

fun KSharpLexer.openSetCurlyBraces(): LexerToken =
    ifChar('[', KSharpTokenType.Operator, KSharpTokenType.OpenSetBracket) {
        if (it.isOperator()) return operator()
        return token(KSharpTokenType.Operator, 1)
    }

fun KSharpLexer.word(type: TokenType): LexerToken =
    loopChar({ isLetter() || isDigit() || equals('_') }, type) {
        if (state.value.consumeLabels && type == KSharpTokenType.LowerCaseWord && it == ':') {
            return token(KSharpTokenType.Label, 0)
        }
        return token(type, 1)
    }

fun KSharpLexer.numberDifferentBase(c: Char): LexerToken? =
    when (c) {
        'x' -> hexNumber()
        'b' -> binaryNumber()
        'o' -> octalNumber()
        else -> null
    }

fun KSharpLexer.number(firstLetterIsZero: Boolean): LexerToken {
    var started = false
    while (true) {
        val c = this.nextChar() ?: return token(KSharpTokenType.Integer, 1)
        if (firstLetterIsZero && !started) {
            started = true
            val result = numberDifferentBase(c)
            if (result != null) return result
        }
        if (c == '_') continue
        if (c.isDot()) return decimal(KSharpTokenType.Integer, 2)
        if (!c.isDigit()) {
            return token(KSharpTokenType.Integer, 1)
        }
    }
}

private fun KSharpLexer.numberInDifferentBase(type: KSharpTokenType, expectedDigits: Set<Char>): LexerToken {
    var start = false
    var skip = 2
    while (true) {
        val c = this.nextChar() ?: return if (start) token(
            type,
            1
        ) else token(KSharpTokenType.Integer, skip)
        skip += 1
        if (c == '_') continue
        if (!expectedDigits.contains(c)) {
            if (!start) return token(KSharpTokenType.Integer, 1)
            return token(type, 1)
        }
        start = true
    }
}

fun KSharpLexer.hexNumber(): LexerToken = numberInDifferentBase(KSharpTokenType.HexInteger, hexLetterDigit)

fun KSharpLexer.binaryNumber(): LexerToken = numberInDifferentBase(KSharpTokenType.BinaryInteger, binaryDigit)

fun KSharpLexer.octalNumber(): LexerToken = numberInDifferentBase(KSharpTokenType.OctalInteger, octalLetterDigit)

fun KSharpLexer.decimal(type: TokenType, skip: Int): LexerToken {
    var start = false
    while (true) {
        val c = this.nextChar() ?: return token(if (start) KSharpTokenType.Float else type, if (start) 1 else skip)
        if (!c.isDigit()) {
            if (!start) return token(type, skip)
            return token(KSharpTokenType.Float, 1)
        }
        start = true
    }
}

fun KSharpLexer.character(): LexerToken =
    ifChar({ equals('\\') }, KSharpTokenType.Character, {
        loopChar({ !equals('\'') }, KSharpTokenType.Character)
    }) {
        if (it == '\'') token(KSharpTokenType.Character, 0)
        else when (this.nextChar()) {
            '\'' -> token(KSharpTokenType.Character, 0)
            else -> token(KSharpTokenType.Character, 1)
        }
    }

private class StringLexerInfo {
    private var begin: Boolean = false
    var endQuotesRequired: Int = 1
        private set
    var isEscapeCharacter: Boolean = false
        private set

    var lastIsQuote: Boolean = false
        private set

    fun process(c: Char) {
        lastIsQuote = c == '"'
        isEscapeCharacter = c == '\\' && !isEscapeCharacter
        if (c == '"' && !isEscapeCharacter) {
            endQuotesRequired += if (!begin) 1 else -1
        }
    }

    fun startStringContent() {
        begin = true
    }

}

fun KSharpLexer.string(): LexerToken {
    val info = StringLexerInfo()
    info.process(this.nextChar() ?: return token(KSharpTokenType.String, 1))
    info.process(this.nextChar() ?: return token(KSharpTokenType.String, 1))

    if (info.endQuotesRequired == 2) return token(KSharpTokenType.String, if (info.lastIsQuote) 0 else 1)
    val tokenType = if (info.endQuotesRequired == 3) KSharpTokenType.MultiLineString else KSharpTokenType.String

    info.startStringContent()
    while (info.endQuotesRequired > 0) {
        info.process(this.nextChar() ?: return token(tokenType, 1))
    }
    return token(tokenType, 0)
}


fun KSharpLexer.newLine(): LexerToken = loopChar({ isSpace() }, KSharpTokenType.NewLine)

fun KSharpLexer.whiteSpace(): LexerToken = loopChar({ isSpace() }, KSharpTokenType.WhiteSpace)


val kSharpTokenFactory: TokenFactory<KSharpLexerState> = { c ->
    with(c) {
        when {
            isLetter() ->
                word(
                    if (isUpperCase()) KSharpTokenType.UpperCaseWord
                    else KSharpTokenType.LowerCaseWord
                )

            equals('#') -> openSetCurlyBraces()
            equals('\'') -> character()
            equals('"') -> string()
            isDigit() -> number(c == '0')
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

private fun Token.shouldDiscardToken(discardBlockTokens: Boolean, blockCounter: AtomicInteger): Boolean {
    if (discardBlockTokens) {
        if (type == KSharpTokenType.BeginBlock) {
            blockCounter.incrementAndGet()
            return true
        }
        if (type == KSharpTokenType.EndBlock) {
            if (blockCounter.get() == 0) {
                return false
            }
            blockCounter.decrementAndGet()
            return true
        }
    }
    return false
}

private fun KSharpLexerIterator.discardBlocksOrNewLineTokens(): KSharpLexerIterator {
    val blockCounter = AtomicInteger(0)
    return generateLexerIterator(state) {
        while (hasNext()) {
            val item = next()
            if (item.type == KSharpTokenType.NewLine && state.value.discardNewLineToken) {
                continue
            }
            if (item.shouldDiscardToken(state.value.discardBlockTokens, blockCounter)) {
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