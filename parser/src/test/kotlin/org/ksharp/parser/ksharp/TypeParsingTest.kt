package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.nodes.TempNode
import org.ksharp.nodes.TraitFunctionNode
import org.ksharp.nodes.TraitFunctionsNode
import org.ksharp.parser.BaseParserErrorCode
import org.ksharp.parser.LexerToken
import org.ksharp.parser.TextToken
import org.ksharp.parser.asSequence
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight

class TypeParserTest : StringSpec({
    "Invalid type separator" {
        "type ListOfInt = List -- Int"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
            .shouldBeLeft()
            .mapLeft {
                (it.error to it.remainTokens.asSequence().toList())
            }.shouldBeLeft(
                BaseParserErrorCode.ExpectingToken.new(
                    "token" to "<EndBlock>",
                    "received-token" to "Operator3:--"
                ) to listOf(
                    LexerToken(
                        type = KSharpTokenType.Operator3,
                        token = TextToken(text = "--", startOffset = 22, endOffset = 23)
                    ),
                    LexerToken(
                        type = KSharpTokenType.UpperCaseWord,
                        token = TextToken(text = "Int", startOffset = 25, endOffset = 27)
                    ),
                    LexerToken(
                        type = KSharpTokenType.NewLine,
                        token = TextToken(text = "", startOffset = 0, endOffset = 0)
                    ),
                    LexerToken(
                        type = KSharpTokenType.EndBlock,
                        token = TextToken(text = "", startOffset = 0, endOffset = 0)
                    )
                )
            )
    }
    "Invalid type separator 2" {
        "type Bool = True |- False"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
            .shouldBeLeft()
            .mapLeft {
                (it.error to it.remainTokens.asSequence().toList())
            }.shouldBeLeft(
                BaseParserErrorCode.ExpectingToken.new(
                    "token" to "<EndBlock>",
                    "received-token" to "Operator9:|-"
                ) to listOf(
                    LexerToken(
                        type = KSharpTokenType.Operator9,
                        token = TextToken(text = "|-", startOffset = 17, endOffset = 18)
                    ),
                    LexerToken(
                        type = KSharpTokenType.UpperCaseWord,
                        token = TextToken(text = "False", startOffset = 20, endOffset = 24)
                    ),
                    LexerToken(
                        type = KSharpTokenType.NewLine,
                        token = TextToken(text = "", startOffset = 0, endOffset = 0)
                    ),
                    LexerToken(
                        type = KSharpTokenType.EndBlock,
                        token = TextToken(text = "", startOffset = 0, endOffset = 0)
                    )
                )
            )
    }
    "Invalid type separator 3" {
        "type Bool = True &- False"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
            .shouldBeLeft()
            .mapLeft {
                (it.error to it.remainTokens.asSequence().toList())
            }.shouldBeLeft(
                BaseParserErrorCode.ExpectingToken.new(
                    "token" to "<EndBlock>",
                    "received-token" to "Operator7:&-"
                ) to listOf(
                    LexerToken(
                        type = KSharpTokenType.Operator7,
                        token = TextToken(text = "&-", startOffset = 17, endOffset = 18)
                    ),
                    LexerToken(
                        type = KSharpTokenType.UpperCaseWord,
                        token = TextToken(text = "False", startOffset = 20, endOffset = 24)
                    ),
                    LexerToken(
                        type = KSharpTokenType.NewLine,
                        token = TextToken(text = "", startOffset = 0, endOffset = 0)
                    ),
                    LexerToken(
                        type = KSharpTokenType.EndBlock,
                        token = TextToken(text = "", startOffset = 0, endOffset = 0)
                    )
                )
            )
    }
    "Type using parenthesis" {
        "type ListOfInt = (List Int)"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
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
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
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
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
            .map { it.value }
            .shouldBeRight(TempNode(listOf("type", "Integer", TempNode(listOf("Int")))))
    }
    "Internal Alias type" {
        "internal type Integer = Int"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
            .map { it.value }
            .shouldBeRight(TempNode(listOf("internal", TempNode(listOf("type", "Integer", TempNode(listOf("Int")))))))
    }
    "Parametric alias type" {
        "type ListOfInt = List Int"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
            .map { it.value }
            .shouldBeRight(TempNode(listOf("type", "ListOfInt", TempNode(listOf("List", TempNode(listOf("Int")))))))
    }
    "Parametric type" {
        "type KVStore k v = Map k v"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
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
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
            .map { it.value }
            .shouldBeRight(TempNode(listOf("type", "Num", "n", TempNode(listOf("n")))))
    }
    "Function type" {
        "type Sum a = a -> a -> a"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
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
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
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
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
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
    "Internal function type" {
        "internal type ToString a = a -> String"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
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
    "Union type" {
        "type Bool = True | False"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
            .map { it.value }
            .shouldBeRight(
                TempNode(
                    listOf(
                        "type",
                        "Bool",
                        TempNode(
                            listOf(
                                TempNode(
                                    listOf(
                                        "True"
                                    )
                                ),
                                TempNode(
                                    listOf(
                                        "|",
                                        TempNode(
                                            listOf(
                                                "False"
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
    }
    "Intersection type" {
        "type Num = Eq & Ord"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
            .map { it.value }
            .shouldBeRight(
                TempNode(
                    listOf(
                        "type",
                        "Num",
                        TempNode(
                            listOf(
                                TempNode(
                                    listOf(
                                        "Eq"
                                    )
                                ),
                                TempNode(
                                    listOf(
                                        "&",
                                        TempNode(
                                            listOf(
                                                "Ord"
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
    }
    "Trait types" {
        """
            trait Num a =
                sum :: a -> a -> a
                prod :: a -> a -> a
        """.trimIndent()
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
            .map { it.value }
            .shouldBeRight(
                TempNode(
                    list = listOf(
                        "trait",
                        "Num",
                        "a",
                        TraitFunctionsNode(
                            listOf(
                                TraitFunctionNode(
                                    "sum",
                                    TempNode(
                                        list = listOf(
                                            "a",
                                            "->",
                                            TempNode(list = listOf("a", "->", TempNode(list = listOf("a"))))
                                        )
                                    ),
                                    Location.NoProvided
                                ),
                                TraitFunctionNode(
                                    "prod",
                                    TempNode(
                                        list = listOf(
                                            "a",
                                            "->",
                                            TempNode(list = listOf("a", "->", TempNode(list = listOf("a"))))
                                        )
                                    ),
                                    Location.NoProvided
                                )
                            )
                        )
                    )
                )
            )
    }
    "Labels on parametric types" {
        "type KVStore k v = Map key: k value: v"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
            .map { it.value }
            .shouldBeRight(
                TempNode(
                    listOf(
                        "type", "KVStore", "k", "v", TempNode(
                            listOf(
                                "Map",
                                TempNode(
                                    listOf(
                                        "key:", "k",
                                        TempNode(listOf("value:", "v"))
                                    )
                                )
                            )
                        )
                    )
                )
            )
    }
    "Labels on tuples" {
        "type Point2D = x: Double, y: Double"
            .kSharpLexer()
            .collapseKSharpTokens()
            .markBlocks { LexerToken(it, TextToken("", 0, 0)) }
            .consumeBlock(KSharpLexerIterator::consumeTypeDeclaration)
            .map { it.value }
            .shouldBeRight(
                TempNode(
                    listOf(
                        "type", "Point2D", TempNode(
                            listOf(
                                "x:", "Double", ",",
                                TempNode(
                                    listOf(
                                        "y:", "Double",
                                    )
                                )
                            )
                        )
                    )
                )
            )
    }
})