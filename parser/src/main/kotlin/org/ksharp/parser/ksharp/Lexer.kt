package org.ksharp.parser.ksharp

import org.ksharp.common.*
import org.ksharp.nodes.AnnotationNode
import org.ksharp.parser.*
import java.io.Reader

data class KSharpLexerState(
    val lastError: ResettableValue<Error> = resettableValue(),
    val indentationOffset: IndentationOffset = IndentationOffset(),
    val lineOffset: LineOffset = LineOffset(),
    val emitLocations: Boolean = false,
    val traitAnnotations: ResettableListBuilder<AnnotationNode> = resettableListBuilder(),
    val annotations: ResettableListBuilder<AnnotationNode> = resettableListBuilder(),
    val consumeLabels: Boolean = false,
    val collapseDotOperatorRule: Boolean = true,
    val collapseAssignOperatorRule: Boolean = true
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

    UnitValue,

    //Keywords
    If,
    Let,
    Match,
    Then,
    Else,
    With,
    Lambda,
    UnitLambda
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

private val operators = "+-*/%><=!&$#^?.\\|:".toSet()
private val hexLetterDigit = "0123456789abcdefABCDEF".toSet()
private val decimalDigit = "0123456789".toSet()
private val octalLetterDigit = "01234567".toSet()
private val binaryDigit = "01".toSet()
private val escapeCharacters = "t'\"rnf\\b".toSet()

fun Char.isLetter() = isLowerCase() || isUpperCase() || isTitleCase()

fun Char.isDigit() = decimalDigit.contains(this)

fun Char.isEscapeCharacter() = escapeCharacters.contains(this)

fun Char.isNewLine() = this == '\n' || this == '\r'

fun Char.isSpace() = this == ' ' || this == '\t'

fun Char.isOperator() = operators.contains(this)

fun Char.isDot() = this == '.'

fun shouldIgnore() = false

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
            return token(BaseTokenType.NewLine, 1)
        }
    }
    while (true) {
        val c = this.nextChar() ?: return token(BaseTokenType.NewLine, 1)
        if (c.isNewLine()) {
            return token(BaseTokenType.IgnoreNewLine, 1)
        }
        if (!c.isSpace())
            return token(BaseTokenType.NewLine, 1)
    }
}

fun KSharpLexer.whiteSpace(): LexerToken = loopChar({ isSpace() }, BaseTokenType.WhiteSpace)

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

private fun isLambda(current: Token, newToken: Token): Boolean =
    current.type == KSharpTokenType.Operator && current.text == "\\" && newToken.type == KSharpTokenType.LowerCaseWord

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
            text == "\\->" -> new(type = KSharpTokenType.UnitLambda)
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

fun KSharpLexerIterator.ensureNewLineAtEnd(): KSharpLexerIterator {
    var lastTokenIsNewLine = false
    var lastToken: Token? = null
    return generateLexerIterator(state) {
        if (hasNext()) {
            lastToken = next()
            lastTokenIsNewLine = lastToken!!.type == BaseTokenType.NewLine
            lastToken
        } else if (!lastTokenIsNewLine) {
            lastTokenIsNewLine = true
            val offset = lastToken!!.endOffset + 1
            LexerToken(
                BaseTokenType.NewLine,
                token = TextToken("\n", offset, offset)
            )
        } else null
    }.cast()
}

private fun Token.mapToKeyword(): Token =
    when (text) {
        "if" -> {
            new(KSharpTokenType.If)
        }

        "let" -> {
            new(KSharpTokenType.Let)
        }

        "match" -> {
            new(KSharpTokenType.Match)
        }

        "then" -> {
            new(KSharpTokenType.Then)
        }

        "else" -> {
            new(KSharpTokenType.Else)
        }

        "with" -> {
            new(KSharpTokenType.With)
        }

        else -> this
    }

private fun KSharpLexerIterator.mapKeywords(): KSharpLexerIterator =
    generateLexerIterator(state) {
        if (hasNext()) {
            val token = next()
            if (token.type == KSharpTokenType.LowerCaseWord) {
                token.mapToKeyword()
            } else token
        } else null
    }

fun KSharpLexerIterator.collapseTokensExceptNewLines(): KSharpLexerIterator =
    collapseTokens(BaseTokenType.NewLine, KSharpTokenType.OpenParenthesis, KSharpTokenType.CloseParenthesis)

fun KSharpLexerIterator.collapseKSharpTokens(): KSharpLexerIterator {
    var lastToken: Token? = null
    return generateLexerIterator(state) {
        var token: Token? = lastToken
        lastToken = null
        while (hasNext()) {
            if (token == null) {
                token = next()
                continue
            }

            lastToken = next()
            val nextToken = lastToken!!

            if (isLambda(token, nextToken)) {
                token = token.collapse(
                    KSharpTokenType.Lambda,
                    nextToken.text,
                    nextToken
                )
                continue
            }

            if (isUnitToken(token, nextToken)) {
                token = token.collapse(
                    KSharpTokenType.UnitValue,
                    "()",
                    nextToken
                )
                continue
            }

            if (canCollapseTokens(token, nextToken)) {
                token = token.collapse(
                    KSharpTokenType.FunctionName,
                    "${token.text}${nextToken.text}",
                    nextToken
                )
                continue
            }

            break
        }
        token?.mapOperatorToken()
    }.mapKeywords()
}

fun KSharpLexerIterator.filterAndCollapseTokens() =
    ensureNewLineAtEnd()
        .collapseTokensExceptNewLines()
        .collapseKSharpTokens()
        .filterWhiteSpace()

fun String.kSharpLexer() = lexer(KSharpLexerState(), charStream(), kSharpTokenFactory).filter {
    it.type != KSharpTokenType.Ignore
}

fun Reader.kSharpLexer() = lexer(KSharpLexerState(), charStream(), kSharpTokenFactory).filter {
    it.type != KSharpTokenType.Ignore
}
