package org.ksharp.parser

import io.kotest.core.spec.style.StringSpec
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight

enum class TestParserTokenTypes : TokenType {
    Test1,
    Keyword,
    Operator
}

private fun keyword(text: String) = LexerToken(TestParserTokenTypes.Keyword, TextToken(text, 0, 0))
private fun point() = LexerToken(TestParserTokenTypes.Operator, TextToken(".", 0, 0))
private fun pcomma() = LexerToken(TestParserTokenTypes.Operator, TextToken(";", 0, 0))
class ParserTest : StringSpec({
    "Given a lexer iterator, consume tokens and produce a Node" {
        generateSequence {
            LexerToken(BaseTokenType.Unknown, TextToken("1", 0, 0))
        }.take(5).iterator()
            .consume(BaseTokenType.Unknown)
            .then(BaseTokenType.Unknown)
            .then(BaseTokenType.Unknown)
            .build {
                it.asSequence().map { n ->
                    n as LexerToken
                    n.text.toInt()
                }.sum()
            }.map {
                it.value to it.remainTokens.asSequence().toList()
            }.shouldBeRight(
                3 to listOf(
                    LexerToken(BaseTokenType.Unknown, TextToken("1", 0, 0)),
                    LexerToken(BaseTokenType.Unknown, TextToken("1", 0, 0))
                )
            )
    }

    "Given a lexer iterator, fail first type and then consume another rule" {
        generateSequence {
            LexerToken(BaseTokenType.Unknown, TextToken("1", 0, 0))
        }.take(5).iterator()
            .consume(TestParserTokenTypes.Test1)
            .build { it as Any }
            .also { it.shouldBeLeft() }
            .or {
                it.consume(BaseTokenType.Unknown)
                    .then(BaseTokenType.Unknown)
                    .then(BaseTokenType.Unknown)
                    .build { data ->
                        data.asSequence().map { n ->
                            n as LexerToken
                            n.text.toInt()
                        }.sum()
                    }
            }
            .map {
                it.value to it.remainTokens.asSequence().toList()
            }.shouldBeRight(
                3 to listOf(
                    LexerToken(BaseTokenType.Unknown, TextToken("1", 0, 0)),
                    LexerToken(BaseTokenType.Unknown, TextToken("1", 0, 0))
                )
            )
    }

    "Given a lexer iterator, consume tokens, persisting last token" {
        listOf(
            keyword("kotlin"),
            point(),
            keyword("sequence"),
            pcomma()
        ).iterator()
            .consume(TestParserTokenTypes.Keyword)
            .thenLoop {
                it.consume(TestParserTokenTypes.Operator, ".")
                    .then(TestParserTokenTypes.Keyword)
                    .build { v ->
                        v.joinToString("") { i ->
                            i as LexerToken
                            i.text
                        }
                    }
            }
            .build {
                it.joinToString("") { v ->
                    when (v) {
                        is String -> v
                        is LexerToken -> v.text
                        else -> ""
                    }
                }
            }.map {
                it.value to it.remainTokens.asSequence().toList()
            }.shouldBeRight("kotlin.sequence" to listOf(pcomma()))

    }
    "Given a lexer iterator, consume tokens, then resume" {
        listOf(
            keyword("kotlin"),
            point(),
            keyword("sequence"),
            pcomma()
        ).iterator()
            .consume(TestParserTokenTypes.Keyword)
            .thenLoop {
                it.consume(TestParserTokenTypes.Operator, ".")
                    .then(TestParserTokenTypes.Keyword)
                    .build { v ->
                        v.joinToString("") { i ->
                            i as LexerToken
                            i.text
                        }
                    }
            }.build {
                it.joinToString("") { v ->
                    when (v) {
                        is String -> v
                        is LexerToken -> v.text
                        else -> ""
                    }
                }
            }.resume()
            .then(TestParserTokenTypes.Operator, ";")
            .build {
                it.joinToString("") { v ->
                    when (v) {
                        is String -> v
                        is LexerToken -> v.text
                        else -> ""
                    }
                }
            }
            .map {
                it.value to it.remainTokens.asSequence().toList()
            }.shouldBeRight("kotlin.sequence;" to emptyList())
    }
    "Given a lexer iterator, consume token, then consume a collection of tokens" {
        listOf(
            keyword("import"),
            keyword("kotlin"),
            point(),
            keyword("sequence"),
            pcomma(),
            pcomma()
        ).iterator()
            .consume(TestParserTokenTypes.Keyword, "import")
            .consume {
                it.consume(TestParserTokenTypes.Keyword)
                    .thenLoop { lexer ->
                        lexer.consume(TestParserTokenTypes.Operator, ".")
                            .then(TestParserTokenTypes.Keyword)
                            .build { v ->
                                v.joinToString("") { i ->
                                    i as LexerToken
                                    i.text
                                }
                            }
                    }.build { data ->
                        data.joinToString("") { v ->
                            when (v) {
                                is String -> v
                                is LexerToken -> v.text
                                else -> ""
                            }
                        }
                    }
            }
            .then(TestParserTokenTypes.Operator, ";")
            .then(TestParserTokenTypes.Operator, ";")
            .build {
                it.joinToString(" --- ") { v ->
                    when (v) {
                        is String -> v
                        is LexerToken -> v.text
                        else -> ""
                    }
                }
            }
            .map {
                it.value to it.remainTokens.asSequence().toList()
            }.shouldBeRight("import --- kotlin.sequence --- ; --- ;" to emptyList())
    }
})