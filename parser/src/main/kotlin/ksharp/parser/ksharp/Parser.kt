package ksharp.parser.ksharp

import ksharp.parser.LexerToken
import ksharp.parser.WithNodeCollector
import ksharp.parser.consume
import ksharp.parser.then

fun Iterator<LexerToken>.consumeDot() = consume(KSharpTokenType.Operator, ".")
fun Iterator<LexerToken>.consumeLowerCaseWord(text: String? = null) = if (text != null) {
    consume(KSharpTokenType.LowerCaseWord, text)
} else consume(KSharpTokenType.LowerCaseWord)

fun WithNodeCollector.thenLowerCaseWord(text: String? = null) = if (text != null) {
    then(KSharpTokenType.LowerCaseWord, text)
} else then(KSharpTokenType.LowerCaseWord)