package org.ksharp.parser.ksharp

import org.ksharp.common.*
import org.ksharp.nodes.AnnotationNode
import org.ksharp.parser.*
import java.io.Reader
import java.util.concurrent.atomic.AtomicInteger

data class KSharpLexerState(
    val lastError: ResettableValue<Error> = resettableValue(),
    val emitLocations: Boolean = false,
    val annotations: ResettableListBuilder<AnnotationNode> = resettableListBuilder(),
    val consumeLabels: Boolean = false,
    val discardBlockTokens: Boolean = false,
    val discardNewLineToken: Boolean = false,
    val collapseDotOperatorRule: Boolean = true,
    val collapseAssignOperatorRule: Boolean = true,
    val mapThenElseKeywords: Boolean = false,
    val mapThenKeywords: Boolean = false,
    val enableExpressionStartingNewLine: Boolean = true
)

typealias KSharpLexer = Lexer<KSharpLexerState>
typealias KSharpLexerIterator = BaseLexerIterator<KSharpLexerState>

enum class KSharpTokenType : TokenType {
    Ignore,
    UpperCaseWord,
    LowerCaseWord,
    Label,
    FunctionName,
    OperatorFunctionName,
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
    AssignOperator,

    Operator0,
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
    EndBlock,

    UnitValue,

    //?Keywords
    If,
    Let,
    Then,
    Else
}

private val mappings = mapOf(
    '@' to KSharpTokenType.Alt,
    ',' to KSharpTokenType.Comma,
    '[' to KSharpTokenType.OpenBracket,
    ']' to KSharpTokenType.CloseBracket,
    ')' to KSharpTokenType.CloseParenthesis,
    '{' to KSharpTokenType.OpenCurlyBraces,
    '}' to KSharpTokenType.CloseCurlyBraces
)

private val ifKeywordsMapping = mapOf(
    "then" to KSharpTokenType.Then,
    "else" to KSharpTokenType.Else
)

private val letKeywordsMapping = mapOf(
    "then" to KSharpTokenType.Then
)

private val operators = "+-*/%><=!&$#^?.\\|:".toSet()
private val hexLetterDigit = "0123456789abcdefABCDEF".toSet()
private val octalLetterDigit = "01234567".toSet()
private val binaryDigit = "01".toSet()
private val escapeCharacters = "t'\"rnf\\b".toSet()

fun Char.isEscapeCharacter() = escapeCharacters.contains(this)

fun Char.isNewLine() = this == '\n' || this == '\r'

fun Char.isSpace() = this == ' ' || this == '\t'

fun Char.isOperator() = operators.contains(this)
fun Char.isDot() = this == '.'

fun Char.shouldIgnore() = false

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

fun <R> KSharpLexerIterator.emitLocations(withLocations: Boolean, code: (KSharpLexerIterator) -> R): R =
    state.value.emitLocations.let { initValue ->
        state.update(state.value.copy(emitLocations = withLocations))
        code(this).also {
            state.update(state.value.copy(emitLocations = initValue))
        }
    }

fun <R> KSharpLexerIterator.disableExpressionStartingNewLine(code: (KSharpLexerIterator) -> R): R =
    state.value.enableExpressionStartingNewLine.let { initValue ->
        state.update(state.value.copy(enableExpressionStartingNewLine = false))
        code(this).also {
            state.update(state.value.copy(enableExpressionStartingNewLine = initValue))
        }
    }

fun <R> KSharpLexerIterator.enableExpressionStartingNewLine(code: (KSharpLexerIterator) -> R): R =
    state.value.enableExpressionStartingNewLine.let { initValue ->
        state.update(state.value.copy(enableExpressionStartingNewLine = true))
        code(this).also {
            state.update(state.value.copy(enableExpressionStartingNewLine = initValue))
        }
    }


fun <R> KSharpLexerIterator.disableCollapseDotOperatorRule(code: (KSharpLexerIterator) -> R): R =
    state.value.collapseDotOperatorRule.let { initValue ->
        state.update(state.value.copy(collapseDotOperatorRule = false))
        code(this).also {
            state.update(state.value.copy(collapseDotOperatorRule = initValue))
        }
    }

fun <R> KSharpLexerIterator.disableCollapseAssignOperatorRule(code: (KSharpLexerIterator) -> R): R =
    state.value.collapseAssignOperatorRule.let { initValue ->
        state.update(state.value.copy(collapseAssignOperatorRule = false))
        code(this).also {
            state.update(state.value.copy(collapseAssignOperatorRule = initValue))
        }
    }

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

fun <R> KSharpLexerIterator.disableDiscardNewLineToken(code: (KSharpLexerIterator) -> R): R =
    state.value.discardNewLineToken.let { initValue ->
        state.update(state.value.copy(discardNewLineToken = false))
        code(this).also {
            state.update(state.value.copy(discardNewLineToken = initValue))
        }
    }

fun <R> KSharpLexerIterator.enableDiscardBlockAndNewLineTokens(code: (KSharpLexerIterator) -> R): R =
    enableDiscardBlocksTokens {
        enableDiscardNewLineToken(code)
    }

fun <R> KSharpLexerIterator.enableMapElseThenKeywords(code: (KSharpLexerIterator) -> R): R =
    state.value.mapThenElseKeywords.let { initValue ->
        state.update(state.value.copy(mapThenElseKeywords = true))
        code(this).also {
            state.update(state.value.copy(mapThenElseKeywords = initValue))
        }
    }

fun <R> KSharpLexerIterator.enableMapThenKeywords(code: (KSharpLexerIterator) -> R): R =
    state.value.mapThenKeywords.let { initValue ->
        state.update(state.value.copy(mapThenKeywords = true))
        code(this).also {
            state.update(state.value.copy(mapThenKeywords = initValue))
        }
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


fun KSharpLexer.newLine(requestForNewLineChar: Boolean): LexerToken {
    if (requestForNewLineChar) {
        val nc = nextChar()
        if (nc != '\n' && nc?.isSpace() == false) {
            return token(KSharpTokenType.NewLine, 1)
        }
    }
    return loopChar({ isSpace() }, KSharpTokenType.NewLine)
}

fun KSharpLexer.whiteSpace(): LexerToken = loopChar({ isSpace() }, KSharpTokenType.WhiteSpace)

fun KSharpLexer.openParenthesisOrOperatorFunction(): LexerToken {
    var skip = 0
    while (true) {
        skip += 1
        val c = nextChar() ?: return token(KSharpTokenType.OpenParenthesis, skip)
        if (c == ')' && skip > 1) {
            return token(KSharpTokenType.OperatorFunctionName, 0)
        }
        if (!c.isOperator()) {
            return token(KSharpTokenType.OpenParenthesis, skip)
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

            equals('#') -> openSetCurlyBraces()
            equals('\'') -> character()
            equals('"') -> string()
            equals('(') -> openParenthesisOrOperatorFunction()
            isDigit() -> number(c == '0')
            isDot() -> decimal(KSharpTokenType.Operator, 1)
            isNewLine() -> newLine(this == '\r')
            isSpace() -> whiteSpace()
            isOperator() -> operator()
            shouldIgnore() -> token(KSharpTokenType.Ignore, 0)
            else -> mappings[c]?.let {
                token(it, 0)
            }
        }
    }
}

private fun isUnitToken(current: Token, newToken: Token): Boolean =
    current.type == KSharpTokenType.OpenParenthesis && newToken.type == KSharpTokenType.CloseParenthesis

private fun KSharpLexerIterator.canCollapseTokens(current: Token, newToken: Token): Boolean {
    var collapseDotOperatorRule = false
    val allowedToken = when (current.type) {
        KSharpTokenType.LowerCaseWord -> {
            collapseDotOperatorRule = state.value.collapseDotOperatorRule
            true
        }

        KSharpTokenType.UpperCaseWord -> true
        KSharpTokenType.FunctionName -> true
        else -> false
    }
    if (!allowedToken) return false
    return when (newToken.type) {
        KSharpTokenType.Operator -> {
            when {
                !state.value.collapseAssignOperatorRule && newToken.text == "=" -> false
                collapseDotOperatorRule || newToken.text != "." -> true
                else -> false
            }
        }

        KSharpTokenType.LowerCaseWord -> true
        KSharpTokenType.UpperCaseWord -> true
        KSharpTokenType.FunctionName -> true
        KSharpTokenType.Alt -> true
        else -> false
    }
}

private val operator11 = "*/%".asSequence().toSet()
private val operator10 = "+-".asSequence().toSet()
private val operator8 = "<>".asSequence().toSet()
private val operator7 = "=!".asSequence().toSet()
private val operator6 = "&".asSequence().toSet()
private val operator5 = "^".asSequence().toSet()
private val operator4 = "|".asSequence().toSet()
private val operator1 = "$".asSequence().toSet()
private val operator0 = ".".asSequence().toSet()

/// https://docs.ksharp.org/rfc/syntax#operator-precedence
private fun Token.mapOperatorToken(): Token = when (type) {
    KSharpTokenType.Operator -> {
        when {
            text == "**" -> new(type = KSharpTokenType.Operator12)
            text == "<<" || text == ">>" -> new(type = KSharpTokenType.Operator9)
            text == "&&" -> new(type = KSharpTokenType.Operator3)
            text == "||" -> new(type = KSharpTokenType.Operator2)
            text == "=" -> new(type = KSharpTokenType.AssignOperator)

            text.isEmpty() -> this

            operator11.contains(text.first()) -> new(type = KSharpTokenType.Operator11)
            operator10.contains(text.first()) -> new(type = KSharpTokenType.Operator10)
            operator8.contains(text.first()) -> new(type = KSharpTokenType.Operator8)
            operator7.contains(text.first()) -> new(type = KSharpTokenType.Operator7)
            operator6.contains(text.first()) -> new(type = KSharpTokenType.Operator6)
            operator5.contains(text.first()) -> new(type = KSharpTokenType.Operator5)
            operator4.contains(text.first()) -> new(type = KSharpTokenType.Operator4)
            operator1.contains(text.first()) -> new(type = KSharpTokenType.Operator1)
            operator0.contains(text.first()) -> new(type = KSharpTokenType.Operator0)
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

private fun Token.checkKeywordsCollection(keywords: Map<String, KSharpTokenType>): Token {
    val type = keywords[text]
    return if (type != null) new(type)
    else this
}

private fun Token.mapToKeyword(state: KSharpLexerState): Token =
    when {
        text == "if" -> {
            new(KSharpTokenType.If)
        }

        text == "let" -> {
            new(KSharpTokenType.Let)
        }

        state.mapThenElseKeywords -> {
            checkKeywordsCollection(ifKeywordsMapping)
        }

        state.mapThenKeywords -> {
            checkKeywordsCollection(letKeywordsMapping)
        }

        else -> this
    }

private fun KSharpLexerIterator.mapKeywords(): KSharpLexerIterator =
    generateLexerIterator(state) {
        if (hasNext()) {
            val token = next()
            if (token.type == KSharpTokenType.LowerCaseWord) {
                token.mapToKeyword(state.value)
            } else token
        } else null
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

            if (isUnitToken(token!!, lastToken!!)) {
                token = token!!.collapse(
                    KSharpTokenType.UnitValue,
                    "()",
                    lastToken!!
                )
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
    }.mapKeywords()
}

fun String.kSharpLexer() = lexer(KSharpLexerState(), charStream(), kSharpTokenFactory).filter {
    it.type != KSharpTokenType.Ignore
}

fun Reader.kSharpLexer() = lexer(KSharpLexerState(), charStream(), kSharpTokenFactory).filter {
    it.type != KSharpTokenType.Ignore
}
