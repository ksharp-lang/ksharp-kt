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
            .shouldBeRight(
                TempNode(
                    listOf(
                        "type",
                        "ListOfInt",
                        TempNode(listOf(TempNode(listOf("List", TempNode(listOf("Int"))))))
                    )
                )
            )
    }
    "Function type using parenthesis" {
        "type ListOfInt = (List Int) -> a -> a"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markExpressions { LexerToken(KSharpTokenType.EndExpression, TextToken("", 0, 0)) }
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TempNode(
                    listOf(
                        "type",
                        "ListOfInt",
                        TempNode(
                            listOf(
                                TempNode(listOf("List", TempNode(listOf("Int")))), "->", TempNode(
                                    listOf(
                                        "a", "->", TempNode(
                                            listOf("a")
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
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
    "Internal Alias type" {
        "internal type Integer = Int"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markExpressions { LexerToken(KSharpTokenType.EndExpression, TextToken("", 0, 0)) }
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(TempNode(listOf("internal", TempNode(listOf("type", "Integer", TempNode(listOf("Int")))))))
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
                        TempNode(listOf("a", "->", TempNode(listOf("a", "->", TempNode(listOf("a")))))),
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
                        TempNode(listOf("a", "->", TempNode(listOf("String"))))
                    )
                )
            )
    }
    "Tuple type" {
        "type Point = Double , Double"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markExpressions { LexerToken(KSharpTokenType.EndExpression, TextToken("", 0, 0)) }
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TempNode(
                    listOf(
                        "type",
                        "Point",
                        TempNode(listOf("Double", ",", TempNode(listOf("Double"))))
                    )
                )
            )
    }
    "Intersection type" {
        "type Num = Eq * Ord"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markExpressions { LexerToken(KSharpTokenType.EndExpression, TextToken("", 0, 0)) }
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TempNode(
                    listOf(
                        "type",
                        "Num",
                        TempNode(listOf("Eq", "*", TempNode(listOf("Ord"))))
                    )
                )
            )
    }
    "Internal function type" {
        "internal type ToString a = a -> String"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markExpressions { LexerToken(KSharpTokenType.EndExpression, TextToken("", 0, 0)) }
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TempNode(
                    listOf(
                        "internal",
                        TempNode(
                            listOf(
                                "type",
                                "ToString",
                                "a",
                                TempNode(listOf("a", "->", TempNode(listOf("String"))))
                            )
                        )
                    )
                )
            )
    }
})