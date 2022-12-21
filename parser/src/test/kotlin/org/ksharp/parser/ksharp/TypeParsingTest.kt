package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec
import org.ksharp.nodes.TempNode
import org.ksharp.parser.LexerToken
import org.ksharp.parser.TextToken
import org.ksharp.test.shouldBeRight

class TypeParserTest : StringSpec({
    "Type using parenthesis" {
        "type ListOfInt = (List Int)"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markExpressions { LexerToken(KSharpTokenType.EndExpression, TextToken("", 0, 0)) }
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(TempNode(listOf("type", "ListOfInt", TempNode(listOf("List", TempNode(listOf("Int")))))))
    }
    "Alias type" {
        "type Integer = Int"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markExpressions { LexerToken(KSharpTokenType.EndExpression, TextToken("", 0, 0)) }
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(TempNode(listOf("type", "Integer", TempNode(listOf("Int")))))
    }
    "Parametric alias type" {
        "type ListOfInt = List Int"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markExpressions { LexerToken(KSharpTokenType.EndExpression, TextToken("", 0, 0)) }
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(TempNode(listOf("type", "ListOfInt", TempNode(listOf("List", TempNode(listOf("Int")))))))
    }
    "Parametric type" {
        "type KVStore k v = Map k v"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markExpressions { LexerToken(KSharpTokenType.EndExpression, TextToken("", 0, 0)) }
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TempNode(
                    listOf(
                        "type", "KVStore", "k", "v", TempNode(
                            listOf(
                                "Map", TempNode(
                                    listOf(
                                        "k", TempNode(
                                            listOf("v")
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
    }
    "Parametric type 2" {
        "type Num n = n"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markExpressions { LexerToken(KSharpTokenType.EndExpression, TextToken("", 0, 0)) }
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(TempNode(listOf("type", "Num", "n", TempNode(listOf("n")))))
    }
    "Function type" {
        "type Sum a = a -> a -> a"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markExpressions { LexerToken(KSharpTokenType.EndExpression, TextToken("", 0, 0)) }
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TempNode(
                    listOf(
                        "type",
                        "Sum",
                        "a",
                        TempNode(listOf("a")),
                        TempNode(listOf("->", TempNode(listOf("a")))),
                        TempNode(listOf("->", TempNode(listOf("a"))))
                    )
                )
            )
    }
    "Function type 2" {
        "type ToString a = a -> String"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markExpressions { LexerToken(KSharpTokenType.EndExpression, TextToken("", 0, 0)) }
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TempNode(
                    listOf(
                        "type",
                        "ToString",
                        "a",
                        TempNode(listOf("a")),
                        TempNode(listOf("->", TempNode(listOf("String"))))
                    )
                )
            )
    }
})