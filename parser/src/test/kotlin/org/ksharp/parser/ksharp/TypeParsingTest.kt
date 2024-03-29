package org.ksharp.parser.ksharp

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.nodes.*
import org.ksharp.parser.*
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight

private fun TokenLexerIterator<KSharpLexerState>.prepareLexerForTypeParsing() =
    filterAndCollapseTokens()
        .collapseNewLines()
        .enableLookAhead()
        .enableIndentationOffset()

class TypeParserTest : StringSpec({
    "Invalid type separator" {
        "type ListOfInt = List -- Int"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .resume()
            .then(BaseTokenType.NewLine)
            .shouldBeLeft()
            .mapLeft {
                (it.error to it.remainTokens.asSequence().toList())
            }.shouldBeLeft(
                BaseParserErrorCode.ExpectingToken.new(
                    Location.NoProvided,
                    "token" to "<NewLine>",
                    "received-token" to "Operator10:--"
                ) to listOf(
                    LexerToken(
                        type = KSharpTokenType.Operator10,
                        token = TextToken(text = "--", startOffset = 22, endOffset = 24)
                    ),
                    LexerToken(
                        type = KSharpTokenType.UpperCaseWord,
                        token = TextToken(text = "Int", startOffset = 25, endOffset = 28)
                    ),
                    LexerToken(
                        type = BaseTokenType.NewLine,
                        token = TextToken(text = "\n", startOffset = 29, endOffset = 29)
                    ),
                )
            )
    }
    "Invalid type separator 2" {
        "type Bool = True |- False"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .resume()
            .then(BaseTokenType.NewLine)
            .shouldBeLeft()
            .mapLeft {
                (it.error to it.remainTokens.asSequence().toList())
            }.shouldBeLeft(
                BaseParserErrorCode.ExpectingToken.new(
                    Location.NoProvided,
                    "token" to "<NewLine>",
                    "received-token" to "Operator4:|-"
                ) to listOf(
                    LexerToken(
                        type = KSharpTokenType.Operator4,
                        token = TextToken(text = "|-", startOffset = 17, endOffset = 19)
                    ),
                    LexerToken(
                        type = KSharpTokenType.UpperCaseWord,
                        token = TextToken(text = "False", startOffset = 20, endOffset = 25)
                    ),
                    LexerToken(
                        type = BaseTokenType.NewLine,
                        token = TextToken(text = "\n", startOffset = 26, endOffset = 26)
                    )
                )
            )
    }
    "Invalid type separator 3" {
        "type Bool = True &- False"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .resume()
            .then(BaseTokenType.NewLine)
            .shouldBeLeft()
            .mapLeft {
                (it.error to it.remainTokens.asSequence().toList())
            }.shouldBeLeft(
                BaseParserErrorCode.ExpectingToken.new(
                    Location.NoProvided,
                    "token" to "<NewLine>",
                    "received-token" to "Operator6:&-"
                ) to listOf(
                    LexerToken(
                        type = KSharpTokenType.Operator6,
                        token = TextToken(text = "&-", startOffset = 17, endOffset = 19)
                    ),
                    LexerToken(
                        type = KSharpTokenType.UpperCaseWord,
                        token = TextToken(text = "False", startOffset = 20, endOffset = 25)
                    ),
                    LexerToken(
                        type = BaseTokenType.NewLine,
                        token = TextToken(text = "\n", startOffset = 26, endOffset = 26)
                    )
                )
            )
    }
    "Type using parenthesis" {
        "type ListOfInt = (List Int)"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "ListOfInt",
                    listOf(),
                    ParametricTypeNode(
                        listOf(
                            ConcreteTypeNode("List", Location.NoProvided),
                            ConcreteTypeNode("Int", Location.NoProvided)
                        ), true, Location.NoProvided
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Function type using parenthesis" {
        "type ListOfInt = (List Int) -> a -> a"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "ListOfInt",
                    listOf(),
                    FunctionTypeNode(
                        listOf(
                            ParametricTypeNode(
                                listOf(
                                    ConcreteTypeNode("List", Location.NoProvided),
                                    ConcreteTypeNode("Int", Location.NoProvided)
                                ), true, Location.NoProvided
                            ),
                            ParameterTypeNode("a", Location.NoProvided),
                            ParameterTypeNode(
                                "a", Location.NoProvided
                            )
                        ), Location.NoProvided, FunctionTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Function type using parenthesis 2" {
        "type ListOfInt = (Int -> Int) -> a -> a"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "ListOfInt",
                    listOf(),
                    FunctionTypeNode(
                        listOf(
                            FunctionTypeNode(
                                listOf(
                                    ConcreteTypeNode("Int", Location.NoProvided),
                                    ConcreteTypeNode("Int", Location.NoProvided)
                                ), Location.NoProvided, FunctionTypeNodeLocations(listOf())
                            ),
                            ParameterTypeNode("a", Location.NoProvided),
                            ParameterTypeNode(
                                "a", Location.NoProvided
                            )
                        ), Location.NoProvided, FunctionTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "namespaced type" {
        "type Integer = m.Int"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "Integer",
                    listOf(),
                    ConcreteTypeNode("m.Int", Location.NoProvided),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Alias type" {
        "type Integer = Int"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "Integer",
                    listOf(),
                    ConcreteTypeNode("Int", Location.NoProvided),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Internal Alias type" {
        "internal type Integer = Int"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    true,
                    null,
                    "Integer",
                    listOf(),
                    ConcreteTypeNode("Int", Location.NoProvided),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Parametric alias type" {
        "type ListOfInt = List Int"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "ListOfInt",
                    listOf(),
                    ParametricTypeNode(
                        listOf(
                            ConcreteTypeNode("List", Location.NoProvided),
                            ConcreteTypeNode("Int", Location.NoProvided)
                        ), false, Location.NoProvided
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Parametric type" {
        "type KVStore k v = Map k v"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "KVStore",
                    listOf("k", "v"),
                    ParametricTypeNode(
                        listOf(
                            ConcreteTypeNode("Map", Location.NoProvided),
                            ParameterTypeNode("k", Location.NoProvided),
                            ParameterTypeNode("v", Location.NoProvided)
                        ), false, Location.NoProvided
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Parametric type 2" {
        "type Num n = n"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "Num",
                    listOf("n"),
                    ParameterTypeNode("n", Location.NoProvided),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Parametric type 3" {
        "type Num n = n String"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "Num",
                    listOf("n"),
                    ParametricTypeNode(
                        listOf(
                            ParameterTypeNode("n", Location.NoProvided),
                            ConcreteTypeNode("String", Location.NoProvided)
                        ),
                        false, Location.NoProvided
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Function type" {
        "type Sum a = a -> a -> a"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "Sum",
                    listOf("a"),
                    FunctionTypeNode(
                        listOf(
                            ParameterTypeNode("a", Location.NoProvided),
                            ParameterTypeNode("a", Location.NoProvided),
                            ParameterTypeNode("a", Location.NoProvided)
                        ),
                        Location.NoProvided,
                        FunctionTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Function type 2" {
        "type ToString a = a -> String"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "ToString",
                    listOf("a"),
                    FunctionTypeNode(
                        listOf(
                            ParameterTypeNode("a", Location.NoProvided),
                            ConcreteTypeNode("String", Location.NoProvided)
                        ),
                        Location.NoProvided, FunctionTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Tuple type" {
        "type Point = Double , Double"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "Point",
                    listOf(),
                    TupleTypeNode(
                        listOf(
                            ConcreteTypeNode("Double", Location.NoProvided),
                            ConcreteTypeNode("Double", Location.NoProvided)
                        ),
                        Location.NoProvided,
                        TupleTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Internal function type" {
        "internal type ToString a = a -> String"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    true,
                    null,
                    "ToString",
                    listOf("a"),
                    FunctionTypeNode(
                        listOf(
                            ParameterTypeNode("a", Location.NoProvided),
                            ConcreteTypeNode("String", Location.NoProvided)
                        ),
                        Location.NoProvided, FunctionTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Union type" {
        "type Bool = True | False"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "Bool",
                    listOf(),
                    UnionTypeNode(
                        listOf(
                            ConcreteTypeNode(
                                "True", Location.NoProvided
                            ),
                            ConcreteTypeNode(
                                "False", Location.NoProvided
                            )
                        ), Location.NoProvided, UnionTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Union type 2" {
        "type Bool = True | False |  NoDefined"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "Bool",
                    listOf(),
                    UnionTypeNode(
                        listOf(
                            ConcreteTypeNode(
                                "True", Location.NoProvided
                            ),
                            ConcreteTypeNode(
                                "False", Location.NoProvided
                            ),
                            ConcreteTypeNode(
                                "NoDefined", Location.NoProvided
                            )
                        ), Location.NoProvided, UnionTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Union type 3" {
        "type Maybe a = Just a | Nothing"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "Maybe",
                    listOf("a"),
                    UnionTypeNode(
                        listOf(
                            ParametricTypeNode(
                                listOf(
                                    ConcreteTypeNode(
                                        "Just", Location.NoProvided
                                    ), ParameterTypeNode(
                                        "a", Location.NoProvided
                                    )
                                ), false, Location.NoProvided
                            ),
                            ConcreteTypeNode(
                                "Nothing", Location.NoProvided
                            )
                        ), Location.NoProvided, UnionTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Union type 4" {
        "type Maybe a = a | Nothing"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "Maybe",
                    listOf("a"),
                    UnionTypeNode(
                        listOf(
                            ParameterTypeNode(
                                "a", Location.NoProvided
                            ),
                            ConcreteTypeNode(
                                "Nothing", Location.NoProvided
                            )
                        ), Location.NoProvided, UnionTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Union type 5" {
        "type Maybe = Just a | Nothing"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "Maybe",
                    listOf(),
                    UnionTypeNode(
                        listOf(
                            ParametricTypeNode(
                                listOf(
                                    ConcreteTypeNode(
                                        "Just", Location.NoProvided
                                    ), ParameterTypeNode(
                                        "a", Location.NoProvided
                                    )
                                ), false, Location.NoProvided
                            ),
                            ConcreteTypeNode(
                                "Nothing", Location.NoProvided
                            )
                        ), Location.NoProvided, UnionTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Union type 6" {
        "type Maybe = Just a | Nothing, Name"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "Maybe",
                    listOf(),
                    UnionTypeNode(
                        listOf(
                            ParametricTypeNode(
                                listOf(
                                    ConcreteTypeNode(
                                        "Just", Location.NoProvided
                                    ), ParameterTypeNode(
                                        "a", Location.NoProvided
                                    )
                                ), false, Location.NoProvided
                            ),
                            TupleTypeNode(
                                listOf(
                                    ConcreteTypeNode(
                                        "Nothing", Location.NoProvided
                                    ),
                                    ConcreteTypeNode(
                                        "Name", Location.NoProvided
                                    )
                                ),
                                Location.NoProvided, TupleTypeNodeLocations(listOf())
                            )
                        ), Location.NoProvided, UnionTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Union type 7" {
        "type Maybe = Just a | a Name"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "Maybe",
                    listOf(),
                    UnionTypeNode(
                        listOf(
                            ParametricTypeNode(
                                listOf(
                                    ConcreteTypeNode(
                                        "Just", Location.NoProvided
                                    ), ParameterTypeNode(
                                        "a", Location.NoProvided
                                    )
                                ), false, Location.NoProvided
                            ),
                            ParametricTypeNode(
                                listOf(
                                    ParameterTypeNode(
                                        "a", Location.NoProvided
                                    ),
                                    ConcreteTypeNode(
                                        "Name", Location.NoProvided
                                    )
                                ),
                                false, Location.NoProvided
                            )
                        ), Location.NoProvided, UnionTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Union type 8" {
        "type Maybe a a = Just a | Nothing"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "Maybe",
                    listOf("a", "a"),
                    UnionTypeNode(
                        listOf(
                            ParametricTypeNode(
                                listOf(
                                    ConcreteTypeNode(
                                        "Just", Location.NoProvided
                                    ), ParameterTypeNode(
                                        "a", Location.NoProvided
                                    )
                                ), false, Location.NoProvided
                            ),
                            ConcreteTypeNode(
                                "Nothing", Location.NoProvided
                            )
                        ), Location.NoProvided, UnionTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Invalid Set type 2" {
        "type Bool = True | False & NoDefined"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "Bool",
                    listOf(),
                    InvalidSetTypeNode(
                        Location.NoProvided
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Intersection type" {
        "type Num = Eq & Ord"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "Num",
                    listOf(),
                    IntersectionTypeNode(
                        listOf(
                            ConcreteTypeNode(
                                "Eq", Location.NoProvided
                            ),
                            ConcreteTypeNode(
                                "Ord", Location.NoProvided
                            )
                        ), Location.NoProvided, IntersectionTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Intersection type 2" {
        "type Num = a & Ord"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "Num",
                    listOf(),
                    IntersectionTypeNode(
                        listOf(
                            ParameterTypeNode(
                                "a", Location.NoProvided
                            ),
                            ConcreteTypeNode(
                                "Ord", Location.NoProvided
                            )
                        ), Location.NoProvided, IntersectionTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
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
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TraitNode(
                    false,
                    null,
                    "Num",
                    listOf("a"),
                    TraitFunctionsNode(
                        listOf(
                            TraitFunctionNode(
                                null,
                                "sum",
                                FunctionTypeNode(
                                    listOf(
                                        ParameterTypeNode("a", Location.NoProvided),
                                        ParameterTypeNode("a", Location.NoProvided),
                                        ParameterTypeNode("a", Location.NoProvided)
                                    ),
                                    Location.NoProvided, FunctionTypeNodeLocations(listOf())
                                ),
                                Location.NoProvided, TraitFunctionNodeLocation(Location.NoProvided, Location.NoProvided)
                            ),
                            TraitFunctionNode(
                                null,
                                "prod",
                                FunctionTypeNode(
                                    listOf(
                                        ParameterTypeNode("a", Location.NoProvided),
                                        ParameterTypeNode("a", Location.NoProvided),
                                        ParameterTypeNode("a", Location.NoProvided)
                                    ),
                                    Location.NoProvided, FunctionTypeNodeLocations(listOf())
                                ),
                                Location.NoProvided, TraitFunctionNodeLocation(Location.NoProvided, Location.NoProvided)
                            )
                        ), emptyList()
                    ),
                    Location.NoProvided,
                    TraitNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Trait types 2" {
        """
            trait Num a b =
                sum :: a -> a -> a
                prod :: a -> a -> a
        """.trimIndent()
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TraitNode(
                    false,
                    null,
                    "Num",
                    listOf("a", "b"),
                    TraitFunctionsNode(
                        listOf(
                            TraitFunctionNode(
                                null,
                                "sum",
                                FunctionTypeNode(
                                    listOf(
                                        ParameterTypeNode("a", Location.NoProvided),
                                        ParameterTypeNode("a", Location.NoProvided),
                                        ParameterTypeNode("a", Location.NoProvided)
                                    ),
                                    Location.NoProvided,
                                    FunctionTypeNodeLocations(listOf())
                                ),
                                Location.NoProvided,
                                TraitFunctionNodeLocation(Location.NoProvided, Location.NoProvided)
                            ),
                            TraitFunctionNode(
                                null,
                                "prod",
                                FunctionTypeNode(
                                    listOf(
                                        ParameterTypeNode("a", Location.NoProvided),
                                        ParameterTypeNode("a", Location.NoProvided),
                                        ParameterTypeNode("a", Location.NoProvided)
                                    ),
                                    Location.NoProvided, FunctionTypeNodeLocations(listOf())
                                ),
                                Location.NoProvided,
                                TraitFunctionNodeLocation(Location.NoProvided, Location.NoProvided)
                            )
                        ), emptyList()
                    ),
                    Location.NoProvided,
                    TraitNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Trait types 3" {
        """
            trait Num a  =
                sum :: a -> b -> a
                prod :: a -> a -> a
        """.trimIndent()
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TraitNode(
                    false,
                    null,
                    "Num",
                    listOf("a"),
                    TraitFunctionsNode(
                        listOf(
                            TraitFunctionNode(
                                null,
                                "sum",
                                FunctionTypeNode(
                                    listOf(
                                        ParameterTypeNode("a", Location.NoProvided),
                                        ParameterTypeNode("b", Location.NoProvided),
                                        ParameterTypeNode("a", Location.NoProvided)
                                    ),
                                    Location.NoProvided, FunctionTypeNodeLocations(listOf())
                                ),
                                Location.NoProvided, TraitFunctionNodeLocation(Location.NoProvided, Location.NoProvided)
                            ),
                            TraitFunctionNode(
                                null,
                                "prod",
                                FunctionTypeNode(
                                    listOf(
                                        ParameterTypeNode("a", Location.NoProvided),
                                        ParameterTypeNode("a", Location.NoProvided),
                                        ParameterTypeNode("a", Location.NoProvided)
                                    ),
                                    Location.NoProvided, FunctionTypeNodeLocations(listOf())
                                ),
                                Location.NoProvided, TraitFunctionNodeLocation(Location.NoProvided, Location.NoProvided)
                            )
                        ), emptyList()
                    ),
                    Location.NoProvided,
                    TraitNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Trait types 4" {
        """
            internal trait Num a  =
                sum :: Int -> Int -> Int
        """.trimIndent()
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TraitNode(
                    true,
                    null,
                    "Num",
                    listOf("a"),
                    TraitFunctionsNode(
                        listOf(
                            TraitFunctionNode(
                                null,
                                "sum",
                                FunctionTypeNode(
                                    listOf(
                                        ConcreteTypeNode("Int", Location.NoProvided),
                                        ConcreteTypeNode("Int", Location.NoProvided),
                                        ConcreteTypeNode("Int", Location.NoProvided)
                                    ),
                                    Location.NoProvided, FunctionTypeNodeLocations(listOf())
                                ),
                                Location.NoProvided, TraitFunctionNodeLocation(Location.NoProvided, Location.NoProvided)
                            )
                        ), emptyList()
                    ),
                    Location.NoProvided,
                    TraitNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Labels on parametric types" {
        "type KVStore k v = Map key: k value: v"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "KVStore",
                    listOf("k", "v"),
                    ParametricTypeNode(
                        listOf(
                            ConcreteTypeNode("Map", Location.NoProvided),
                            LabelTypeNode(
                                "key", ParameterTypeNode("k", Location.NoProvided), Location.NoProvided,

                                ),
                            LabelTypeNode(
                                "value",
                                ParameterTypeNode("v", Location.NoProvided),
                                Location.NoProvided,
                            )
                        ), false, Location.NoProvided
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Labels on tuples" {
        "type Point2D = x: Double, y: Double"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "Point2D",
                    listOf(),
                    TupleTypeNode(
                        listOf(
                            LabelTypeNode(
                                "x",
                                ConcreteTypeNode("Double", Location.NoProvided),
                                Location.NoProvided,
                            ),
                            LabelTypeNode(
                                "y",
                                ConcreteTypeNode("Double", Location.NoProvided),
                                Location.NoProvided,
                            )
                        ), Location.NoProvided, TupleTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Labels on tuples with indentation" {
        """type Point2D = x: Double, 
            |             y: Double""".trimMargin()
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "Point2D",
                    listOf(),
                    TupleTypeNode(
                        listOf(
                            LabelTypeNode(
                                "x",
                                ConcreteTypeNode("Double", Location.NoProvided),
                                Location.NoProvided,
                            ),
                            LabelTypeNode(
                                "y",
                                ConcreteTypeNode("Double", Location.NoProvided),
                                Location.NoProvided,
                            )
                        ), Location.NoProvided, TupleTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Labels with composite types" {
        "type Composite a = n: (Num a), point: (x: Double, y: Double)"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "Composite",
                    listOf("a"),
                    TupleTypeNode(
                        listOf(
                            LabelTypeNode(
                                "n", ParametricTypeNode(
                                    listOf(
                                        ConcreteTypeNode("Num", Location.NoProvided),
                                        ParameterTypeNode("a", Location.NoProvided)
                                    ), true, Location.NoProvided
                                ), Location.NoProvided
                            ),
                            LabelTypeNode(
                                "point", TupleTypeNode(
                                    listOf(
                                        LabelTypeNode(
                                            "x",
                                            ConcreteTypeNode("Double", Location.NoProvided),
                                            Location.NoProvided,

                                            ),
                                        LabelTypeNode(
                                            "y",
                                            ConcreteTypeNode("Double", Location.NoProvided),
                                            Location.NoProvided,

                                            )
                                    ), Location.NoProvided, TupleTypeNodeLocations(listOf())
                                ), Location.NoProvided
                            )
                        ), Location.NoProvided, TupleTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Labels with composite types 2 " {
        "type Composite a = n: (), point: (x: Double, y: Double)"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "Composite",
                    listOf("a"),
                    TupleTypeNode(
                        listOf(
                            LabelTypeNode(
                                "n",
                                UnitTypeNode(Location.NoProvided),
                                Location.NoProvided,

                                ),
                            LabelTypeNode(
                                "point",
                                TupleTypeNode(
                                    listOf(
                                        LabelTypeNode(
                                            "x",
                                            ConcreteTypeNode("Double", Location.NoProvided),
                                            Location.NoProvided,

                                            ),
                                        LabelTypeNode(
                                            "y",
                                            ConcreteTypeNode("Double", Location.NoProvided),
                                            Location.NoProvided,

                                            )
                                    ), Location.NoProvided, TupleTypeNodeLocations(listOf())
                                ),
                                Location.NoProvided,
                            )
                        ), Location.NoProvided, TupleTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Labels with composite types 3" {
        "type Composite a = n: (Int -> Int), point: (x: Double, y: Double)"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "Composite",
                    listOf("a"),
                    TupleTypeNode(
                        listOf(
                            LabelTypeNode(
                                "n",
                                FunctionTypeNode(
                                    listOf(
                                        ConcreteTypeNode("Int", Location.NoProvided),
                                        ConcreteTypeNode("Int", Location.NoProvided)
                                    ), Location.NoProvided, FunctionTypeNodeLocations(listOf())
                                ),
                                Location.NoProvided,
                            ),
                            LabelTypeNode(
                                "point",
                                TupleTypeNode(
                                    listOf(
                                        LabelTypeNode(
                                            "x",
                                            ConcreteTypeNode("Double", Location.NoProvided),
                                            Location.NoProvided,

                                            ),
                                        LabelTypeNode(
                                            "y",
                                            ConcreteTypeNode("Double", Location.NoProvided),
                                            Location.NoProvided,

                                            )
                                    ), Location.NoProvided, TupleTypeNodeLocations(listOf())
                                ),
                                Location.NoProvided,
                            )
                        ), Location.NoProvided, TupleTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Constrained type" {
        "type Age = Int => (it > 0) && (it < 70)"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false,
                    null,
                    "Age",
                    listOf(),
                    ConstrainedTypeNode(
                        ConcreteTypeNode("Int", Location.NoProvided),
                        OperatorNode(
                            "Operator3",
                            "&&",
                            OperatorNode(
                                "Operator8",
                                ">",
                                FunctionCallNode("it", FunctionType.Function, listOf(), Location.NoProvided),
                                LiteralValueNode("0", LiteralValueType.Integer, Location.NoProvided),
                                Location.NoProvided
                            ),
                            OperatorNode(
                                "Operator8",
                                "<",
                                FunctionCallNode("it", FunctionType.Function, listOf(), Location.NoProvided),
                                LiteralValueNode("70", LiteralValueType.Integer, Location.NoProvided),
                                Location.NoProvided,
                            ),
                            Location.NoProvided,
                        ),
                        Location.NoProvided, ConstrainedTypeNodeLocations(Location.NoProvided)
                    ),
                    Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Unit type" {
        "type Unit = ()"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeNode(
                    false, null,
                    "Unit", listOf(), UnitTypeNode(Location.NoProvided), Location.NoProvided,
                    TypeNodeLocations(
                        Location.NoProvided,
                        Location.NoProvided,
                        Location.NoProvided,
                        listOf(),
                        Location.NoProvided
                    )
                )
            )
    }
    "Type declaration" {
        "ten :: () -> Int"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeFunctionTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeDeclarationNode(
                    null,
                    "ten",
                    listOf(),
                    FunctionTypeNode(
                        listOf(UnitTypeNode(Location.NoProvided), ConcreteTypeNode("Int", Location.NoProvided)),
                        Location.NoProvided, FunctionTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeDeclarationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
                )
            )
    }
    "If on function type declaration" {
        "if :: () -> Int"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeFunctionTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeDeclarationNode(
                    null,
                    "if",
                    listOf(),
                    FunctionTypeNode(
                        listOf(UnitTypeNode(Location.NoProvided), ConcreteTypeNode("Int", Location.NoProvided)),
                        Location.NoProvided,
                        FunctionTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeDeclarationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
                )
            )
    }
    "Type declaration 2" {
        "sum :: Int -> Int -> Int"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeFunctionTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeDeclarationNode(
                    null,
                    "sum",
                    listOf(),
                    FunctionTypeNode(
                        listOf(
                            ConcreteTypeNode("Int", Location.NoProvided),
                            ConcreteTypeNode("Int", Location.NoProvided),
                            ConcreteTypeNode("Int", Location.NoProvided)
                        ),
                        Location.NoProvided, FunctionTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeDeclarationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
                )
            )
    }
    "Type declaration with params" {
        "sum a :: (Num a) -> (Num a) -> Int"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeFunctionTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeDeclarationNode(
                    null,
                    "sum",
                    listOf("a"),
                    FunctionTypeNode(
                        listOf(
                            ParametricTypeNode(
                                listOf(
                                    ConcreteTypeNode("Num", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided),
                                ),
                                true, Location.NoProvided
                            ),
                            ParametricTypeNode(
                                listOf(
                                    ConcreteTypeNode("Num", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided),
                                ),
                                true, Location.NoProvided
                            ),
                            ConcreteTypeNode("Int", Location.NoProvided)
                        ),
                        Location.NoProvided, FunctionTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeDeclarationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
                )
            )
    }
    "Type declaration with operators" {
        "(+) a :: (Num a) -> (Num a) -> Int"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeFunctionTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeDeclarationNode(
                    null,
                    "(+)",
                    listOf("a"),
                    FunctionTypeNode(
                        listOf(
                            ParametricTypeNode(
                                listOf(
                                    ConcreteTypeNode("Num", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided),
                                ),
                                true, Location.NoProvided
                            ),
                            ParametricTypeNode(
                                listOf(
                                    ConcreteTypeNode("Num", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided),
                                ),
                                true, Location.NoProvided
                            ),
                            ConcreteTypeNode("Int", Location.NoProvided)
                        ),
                        Location.NoProvided, FunctionTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeDeclarationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
                )
            )
    }
    "Nested Parametric Type" {
        "type Val a b = (Seq (Pair a b))"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeTypeDeclaration()
            .map { it.value.also(::println) }
            .shouldBeRight(
                TypeNode(
                    internal = false,
                    annotations = null,
                    name = "Val",
                    params = listOf("a", "b"),
                    expr = ParametricTypeNode(
                        variables = listOf(
                            ConcreteTypeNode(name = "Seq", location = Location.NoProvided),
                            ParametricTypeNode(
                                variables = listOf(
                                    ConcreteTypeNode(name = "Pair", location = Location.NoProvided),
                                    ParameterTypeNode(name = "a", location = Location.NoProvided),
                                    ParameterTypeNode(name = "b", location = Location.NoProvided)
                                ), true, Location.NoProvided
                            )
                        ), true, location = Location.NoProvided
                    ),
                    location = Location.NoProvided,
                    locations = TypeNodeLocations(
                        internalLocation = Location.NoProvided,
                        typeLocation = Location.NoProvided,
                        name = Location.NoProvided,
                        params = emptyList(),
                        assignOperatorLocation = Location.NoProvided
                    )
                )
            )
    }
    "Type declaration complex name with two arguments" {
        "map->sequence a b :: (Map a b) -> (Seq (Pair a b))"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeFunctionTypeDeclaration()
            .map { it.value.also(::println) }
            .shouldBeRight(
                TypeDeclarationNode(
                    annotations = null,
                    name = "map->sequence",
                    params = listOf("a", "b"),
                    type = FunctionTypeNode(
                        params = listOf(
                            ParametricTypeNode(
                                variables = listOf(
                                    ConcreteTypeNode(name = "Map", location = Location.NoProvided),
                                    ParameterTypeNode(name = "a", location = Location.NoProvided),
                                    ParameterTypeNode(name = "b", location = Location.NoProvided)
                                ),
                                closed = true,
                                location = Location.NoProvided
                            ),
                            ParametricTypeNode(
                                variables = listOf(
                                    ConcreteTypeNode(
                                        name = "Seq",
                                        location = Location.NoProvided
                                    ), ParametricTypeNode(
                                        variables = listOf(
                                            ConcreteTypeNode(
                                                name = "Pair",
                                                location = Location.NoProvided
                                            ),
                                            ParameterTypeNode(name = "a", location = Location.NoProvided),
                                            ParameterTypeNode(
                                                name = "b",
                                                location = Location.NoProvided
                                            )
                                        ), closed = true, location = Location.NoProvided
                                    )
                                ), closed = true, location = Location.NoProvided
                            )
                        ),
                        location = Location.NoProvided,
                        locations = FunctionTypeNodeLocations(separators = emptyList())
                    ),
                    location = Location.NoProvided,
                    locations = TypeDeclarationNodeLocations(
                        name = Location.NoProvided,
                        separator = Location.NoProvided,
                        params = emptyList()
                    )
                )
            )
    }
    "Type declaration with complex function names" {
        "wire->internal a :: (Num a) -> (Num a) -> Int"
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeFunctionTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeDeclarationNode(
                    null,
                    "wire->internal",
                    listOf("a"),
                    FunctionTypeNode(
                        listOf(
                            ParametricTypeNode(
                                listOf(
                                    ConcreteTypeNode("Num", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided),
                                ),
                                true, Location.NoProvided
                            ),
                            ParametricTypeNode(
                                listOf(
                                    ConcreteTypeNode("Num", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided),
                                ),
                                true, Location.NoProvided
                            ),
                            ConcreteTypeNode("Int", Location.NoProvided)
                        ),
                        Location.NoProvided, FunctionTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeDeclarationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
                )
            )
    }
    "Type declaration with indentation" {
        """sum :: 
           |  Int -> Int -> Int""".trimMargin()
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeFunctionTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeDeclarationNode(
                    null,
                    "sum",
                    listOf(),
                    FunctionTypeNode(
                        listOf(
                            ConcreteTypeNode("Int", Location.NoProvided),
                            ConcreteTypeNode("Int", Location.NoProvided),
                            ConcreteTypeNode("Int", Location.NoProvided)
                        ),
                        Location.NoProvided, FunctionTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeDeclarationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
                )
            )
    }
    "Type declaration with params and indentation" {
        """sum a :: (Num a) 
            |           -> (Num a) 
            |           -> Int""".trimMargin()
            .kSharpLexer()
            .prepareLexerForTypeParsing()
            .consumeFunctionTypeDeclaration()
            .map { it.value }
            .shouldBeRight(
                TypeDeclarationNode(
                    null,
                    "sum",
                    listOf("a"),
                    FunctionTypeNode(
                        listOf(
                            ParametricTypeNode(
                                listOf(
                                    ConcreteTypeNode("Num", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided),
                                ),
                                true, Location.NoProvided
                            ),
                            ParametricTypeNode(
                                listOf(
                                    ConcreteTypeNode("Num", Location.NoProvided),
                                    ParameterTypeNode("a", Location.NoProvided),
                                ),
                                true, Location.NoProvided
                            ),
                            ConcreteTypeNode("Int", Location.NoProvided)
                        ),
                        Location.NoProvided, FunctionTypeNodeLocations(listOf())
                    ),
                    Location.NoProvided,
                    TypeDeclarationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
                )
            )
    }
})
