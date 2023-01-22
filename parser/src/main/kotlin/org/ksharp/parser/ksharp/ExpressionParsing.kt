package org.ksharp.parser.ksharp

fun KSharpLexerIterator.consumeExpression() =
    consumeLiteral()