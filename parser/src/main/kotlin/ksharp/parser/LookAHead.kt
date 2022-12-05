package ksharp.parser

class LLToken(
    private val lexerToken: LexerToken,
    private val lookAHead: LookAHead
) : LexerToken by lexerToken {


}

class LookAHead internal constructor(
    tokens: Sequence<LexerToken>
) {

}