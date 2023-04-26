package org.ksharp.semantics.expressions

import io.kotest.core.Tuple4
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.module.prelude.preludeModule
import org.ksharp.nodes.*
import org.ksharp.nodes.semantic.*
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.inference.ResolvedTypePromise
import org.ksharp.semantics.inference.TypePromise
import org.ksharp.semantics.inference.getTypePromise
import org.ksharp.semantics.nodes.EmptySemanticInfo
import org.ksharp.semantics.nodes.ModuleTypeSystemInfo
import org.ksharp.semantics.nodes.Symbol
import org.ksharp.semantics.nodes.TypeSemanticInfo
import org.ksharp.semantics.scopes.Function
import org.ksharp.semantics.scopes.FunctionVisibility
import org.ksharp.semantics.scopes.TypeVisibilityTableBuilder
import org.ksharp.typesystem.PartialTypeSystem
import org.ksharp.typesystem.typeSystem
import org.ksharp.typesystem.types.alias
import org.ksharp.typesystem.types.functionType
import org.ksharp.typesystem.types.newParameterForTesting
import org.ksharp.typesystem.types.resetParameterCounterForTesting

private fun typeParameterForTesting(id: Int) = ResolvedTypePromise(newParameterForTesting(id))

private fun module(vararg functions: FunctionNode) =
    ModuleNode(
        "module", listOf(), listOf(), listOf(), listOf(*functions), Location.NoProvided
    )

class FunctionNodeSemanticFunctionTableTest : StringSpec({
    "table: function without declaration" {
        module(
            FunctionNode(
                true,
                "sum",
                listOf("a", "b"),
                OperatorNode(
                    "+",
                    FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                    FunctionCallNode("b", FunctionType.Function, listOf(), Location.NoProvided),
                    Location.NoProvided
                ),
                Location.NoProvided
            )
        ).checkFunctionSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                TypeVisibilityTableBuilder(ErrorCollector()).build(),
                preludeModule.typeSystem
            )
        ).apply {
            errors.shouldBeEmpty()
            functionTable["sum"]
                .shouldNotBeNull()
                .apply {
                    first.shouldBe(
                        Function(
                            FunctionVisibility.Public,
                            "sum",
                            listOf(
                                ResolvedTypePromise(newParameterForTesting(0)),
                                ResolvedTypePromise(newParameterForTesting(1)),
                                ResolvedTypePromise(newParameterForTesting(2)),
                            )
                        )
                    )
                    second.shouldBe(Location.NoProvided)
                }
        }
    }
    "table: function with declaration" {
        val typeSystem = typeSystem(PartialTypeSystem(preludeModule.typeSystem, listOf())) {
            alias("Decl__sum") {
                functionType {
                    type("Int")
                    type("Int")
                    type("Int")
                }
            }
        }.value
        module(
            FunctionNode(
                true,
                "sum",
                listOf("a", "b"),
                OperatorNode(
                    "+",
                    FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                    FunctionCallNode("b", FunctionType.Function, listOf(), Location.NoProvided),
                    Location.NoProvided
                ),
                Location.NoProvided
            )
        ).checkFunctionSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                TypeVisibilityTableBuilder(ErrorCollector()).build(),
                typeSystem
            )
        ).apply {
            errors.shouldBeEmpty()
            functionTable["sum"]
                .shouldNotBeNull()
                .apply {
                    first.shouldBe(
                        Function(
                            FunctionVisibility.Public,
                            "sum",
                            listOf(
                                ResolvedTypePromise(typeSystem.alias("Int").valueOrNull!!),
                                ResolvedTypePromise(typeSystem.alias("Int").valueOrNull!!),
                                ResolvedTypePromise(typeSystem.alias("Int").valueOrNull!!),
                            )
                        )
                    )
                    second.shouldBe(Location.NoProvided)
                }
        }
    }
    "table: function with unit parameters" {
        val typeSystem = preludeModule.typeSystem
        module(
            FunctionNode(
                true,
                "ten",
                listOf(),
                LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                Location.NoProvided
            )
        ).checkFunctionSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                TypeVisibilityTableBuilder(ErrorCollector()).build(),
                typeSystem
            )
        ).apply {
            errors.shouldBeEmpty()
            functionTable["ten"]
                .shouldNotBeNull()
                .apply {
                    first.shouldBe(
                        Function(
                            FunctionVisibility.Public,
                            "ten",
                            listOf(
                                ResolvedTypePromise(typeSystem["Unit"].valueOrNull!!),
                                ResolvedTypePromise(newParameterForTesting(0)),
                            )
                        )
                    )
                    second.shouldBe(Location.NoProvided)
                }
        }
    }
    "table: function with declaration mismatch" {
        val typeSystem = typeSystem(PartialTypeSystem(preludeModule.typeSystem, listOf())) {
            alias("Decl__sum") {
                functionType {
                    type("Int")
                    type("Int")
                }
            }
        }.value
        module(
            FunctionNode(
                true,
                "sum",
                listOf("a", "b"),
                OperatorNode(
                    "+",
                    FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                    FunctionCallNode("b", FunctionType.Function, listOf(), Location.NoProvided),
                    Location.NoProvided
                ),
                Location.NoProvided
            )
        ).checkFunctionSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                TypeVisibilityTableBuilder(ErrorCollector()).build(),
                typeSystem
            )
        ).apply {
            errors.shouldBe(
                listOf(
                    FunctionSemanticsErrorCode.WrongNumberOfParameters.new(
                        Location.NoProvided,
                        "name" to "sum",
                        "fnParams" to 3,
                        "declParams" to 2
                    )
                )
            )
            functionTable["sum"]
                .shouldBeNull()
        }
    }
    "table: function with declaration mismatch 2" {
        val typeSystem = typeSystem(PartialTypeSystem(preludeModule.typeSystem, listOf())) {
            alias("Decl__sum") {
                functionType {
                    type("Int")
                    type("Int")
                }
            }
        }.value
        module(
            FunctionNode(
                true,
                "sum",
                listOf(),
                OperatorNode(
                    "+",
                    FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                    FunctionCallNode("b", FunctionType.Function, listOf(), Location.NoProvided),
                    Location.NoProvided
                ),
                Location.NoProvided
            )
        ).checkFunctionSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                TypeVisibilityTableBuilder(ErrorCollector()).build(),
                typeSystem
            )
        ).apply {
            errors.shouldBe(
                listOf(
                    FunctionSemanticsErrorCode.ParamMismatch.new(
                        Location.NoProvided,
                        "name" to "sum",
                        "fnParam" to "()",
                        "declParam" to "Int"
                    )
                )
            )
            functionTable["sum"]
                .shouldBeNull()
        }
    }
}) {
    override suspend fun beforeAny(testCase: TestCase) {
        super.beforeAny(testCase)
        resetParameterCounterForTesting()
    }
}

class FunctionNodeSemanticTransformSemanticNodeTest : ShouldSpec({
    val ts = preludeModule.typeSystem
    val longTypePromise = ts.getTypePromise("Long")
    val byteTypePromise = ts.getTypePromise("Byte")
    val shortTypePromise = ts.getTypePromise("Short")
    val intTypePromise = ts.getTypePromise("Int")
    val strTypePromise = ts.getTypePromise("String")
    val unitTypePromise = ts.getTypePromise("Unit")
    context("Semantic node: constant function") {
        listOf<Tuple4<LiteralValueType, TypePromise, String, Any>>(
            Tuple4(LiteralValueType.Integer, byteTypePromise, "10", 10.toLong()),
            Tuple4(LiteralValueType.BinaryInteger, byteTypePromise, "0b0001", 1.toLong()),
            Tuple4(LiteralValueType.HexInteger, shortTypePromise, "0xFF", 255.toLong()),
            Tuple4(LiteralValueType.Integer, intTypePromise, "500000", 500000.toLong()),
            Tuple4(LiteralValueType.Integer, longTypePromise, "5000000000", 5000000000),
            Tuple4(LiteralValueType.OctalInteger, byteTypePromise, "0o01", 1.toLong()),
            Tuple4(LiteralValueType.Decimal, ts.getTypePromise("Double"), "1.5", 1.5.toDouble()),
            Tuple4(LiteralValueType.String, strTypePromise, "\"Hello\"", "Hello"),
            Tuple4(LiteralValueType.MultiLineString, strTypePromise, "\"\"\"Hello\nWorld\"\"\"", "Hello\nWorld"),
            Tuple4(LiteralValueType.Character, ts.getTypePromise("Char"), "'c'", 'c'),
        ).forEach { (literalType, expectedType, value, expectedValue) ->
            should("value type $literalType") {
                module(
                    FunctionNode(
                        true,
                        "n",
                        listOf(),
                        LiteralValueNode(value, literalType, Location.NoProvided),
                        Location.NoProvided
                    )
                ).checkFunctionSemantics(
                    ModuleTypeSystemInfo(
                        listOf(),
                        TypeVisibilityTableBuilder(ErrorCollector()).build(),
                        ts
                    )
                ).apply {
                    errors.shouldBeEmpty()
                    abstractions.shouldBe(
                        listOf(
                            AbstractionNode(
                                "n", ConstantNode(
                                    expectedValue,
                                    TypeSemanticInfo(expectedType),
                                    Location.NoProvided
                                ),
                                EmptySemanticInfo,
                                Location.NoProvided
                            )
                        )
                    )
                }
            }
        }
    }
    should("Semantic node: operator") {
        module(
            FunctionNode(
                true,
                "n",
                listOf(),
                OperatorNode(
                    "**",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided
                ),
                Location.NoProvided
            )
        ).checkFunctionSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                TypeVisibilityTableBuilder(ErrorCollector()).build(),
                ts
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        "n",
                        ApplicationNode(
                            ApplicationName(name = "(**)"),
                            listOf(
                                ConstantNode(
                                    10.toLong(),
                                    TypeSemanticInfo(byteTypePromise),
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    2.toLong(),
                                    TypeSemanticInfo(byteTypePromise),
                                    Location.NoProvided
                                )
                            ),
                            TypeSemanticInfo(typeParameterForTesting(1)),
                            Location.NoProvided
                        ),
                        EmptySemanticInfo,
                        Location.NoProvided
                    )
                )
            )
        }
    }
    should("Semantic node: if with else") {
        module(
            FunctionNode(
                true,
                "n",
                listOf("a"),
                IfNode(
                    OperatorNode(
                        ">",
                        LiteralValueNode("4", LiteralValueType.Integer, Location.NoProvided),
                        FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                        Location.NoProvided
                    ),
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("20", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided
                ),
                Location.NoProvided
            )
        ).checkFunctionSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                TypeVisibilityTableBuilder(ErrorCollector()).build(),
                ts
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        "n",
                        ApplicationNode(
                            ApplicationName("::prelude", "if"),
                            listOf(
                                ApplicationNode(
                                    ApplicationName(name = "(>)"),
                                    listOf(
                                        ConstantNode(
                                            4.toLong(),
                                            TypeSemanticInfo(byteTypePromise),
                                            Location.NoProvided
                                        ),
                                        VarNode(
                                            "a",
                                            Symbol("a", typeParameterForTesting(0)),
                                            Location.NoProvided
                                        )
                                    ),
                                    TypeSemanticInfo(typeParameterForTesting(2)),
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    10.toLong(),
                                    TypeSemanticInfo(byteTypePromise),
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    20.toLong(),
                                    TypeSemanticInfo(byteTypePromise),
                                    Location.NoProvided
                                ),
                            ),
                            TypeSemanticInfo(typeParameterForTesting(3)),
                            Location.NoProvided
                        ),
                        EmptySemanticInfo,
                        Location.NoProvided
                    )
                )
            )
        }
    }
    should("Semantic node: if without else") {
        module(
            FunctionNode(
                true,
                "n",
                listOf("a"),
                IfNode(
                    OperatorNode(
                        ">",
                        LiteralValueNode("4", LiteralValueType.Integer, Location.NoProvided),
                        FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                        Location.NoProvided
                    ),
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    UnitNode(Location.NoProvided),
                    Location.NoProvided
                ),
                Location.NoProvided
            )
        ).checkFunctionSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                TypeVisibilityTableBuilder(ErrorCollector()).build(),
                ts
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        "n",
                        ApplicationNode(
                            ApplicationName("::prelude", "if"),
                            listOf(
                                ApplicationNode(
                                    ApplicationName(name = "(>)"),
                                    listOf(
                                        ConstantNode(
                                            4.toLong(),
                                            TypeSemanticInfo(byteTypePromise),
                                            Location.NoProvided
                                        ),
                                        VarNode(
                                            "a",
                                            Symbol("a", typeParameterForTesting(0)),
                                            Location.NoProvided
                                        )
                                    ),
                                    TypeSemanticInfo(typeParameterForTesting(2)),
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    10.toLong(),
                                    TypeSemanticInfo(byteTypePromise),
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    Unit,
                                    TypeSemanticInfo(unitTypePromise),
                                    Location.NoProvided
                                ),
                            ),
                            TypeSemanticInfo(typeParameterForTesting(3)),
                            Location.NoProvided
                        ),
                        EmptySemanticInfo,
                        Location.NoProvided
                    )
                )
            )
        }
    }
    should("Semantic node: list literal") {
        module(
            FunctionNode(
                true,
                "n",
                listOf(),
                LiteralCollectionNode(
                    listOf(
                        LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                        OperatorNode(
                            "+",
                            LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                            LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided
                        )
                    ), LiteralCollectionType.List, Location.NoProvided
                ),
                Location.NoProvided
            )
        ).checkFunctionSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                TypeVisibilityTableBuilder(ErrorCollector()).build(),
                ts
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        "n",
                        ApplicationNode(
                            ApplicationName("::prelude", "listOf"),
                            listOf(
                                ConstantNode(
                                    10.toLong(),
                                    TypeSemanticInfo(byteTypePromise),
                                    Location.NoProvided
                                ),
                                ApplicationNode(
                                    ApplicationName(name = "(+)"),
                                    listOf(
                                        ConstantNode(
                                            2.toLong(),
                                            TypeSemanticInfo(byteTypePromise),
                                            Location.NoProvided
                                        ),
                                        ConstantNode(
                                            1.toLong(),
                                            TypeSemanticInfo(byteTypePromise),
                                            Location.NoProvided
                                        )
                                    ),
                                    TypeSemanticInfo(typeParameterForTesting(1)),
                                    Location.NoProvided
                                )
                            ),
                            TypeSemanticInfo(
                                typeParameterForTesting(2)
                            ),
                            Location.NoProvided
                        ),
                        EmptySemanticInfo,
                        Location.NoProvided
                    )
                )
            )
        }
    }
    should("Semantic node: set literal") {
        module(
            FunctionNode(
                true,
                "n",
                listOf(),
                LiteralCollectionNode(
                    listOf(
                        LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                        LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                        LiteralValueNode("3", LiteralValueType.Integer, Location.NoProvided)
                    ),
                    LiteralCollectionType.Set,
                    Location.NoProvided
                ),
                Location.NoProvided
            )
        ).checkFunctionSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                TypeVisibilityTableBuilder(ErrorCollector()).build(),
                ts
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        "n",
                        ApplicationNode(
                            ApplicationName("::prelude", "setOf"),
                            listOf(
                                ConstantNode(
                                    1.toLong(),
                                    TypeSemanticInfo(byteTypePromise),
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    2.toLong(),
                                    TypeSemanticInfo(byteTypePromise),
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    3.toLong(),
                                    TypeSemanticInfo(byteTypePromise),
                                    Location.NoProvided
                                )
                            ),
                            TypeSemanticInfo(
                                typeParameterForTesting(1)
                            ),
                            Location.NoProvided
                        ),
                        EmptySemanticInfo,
                        Location.NoProvided
                    )
                )
            )
        }
    }
    should("Semantic node: tuple literal") {
        module(
            FunctionNode(
                true,
                "n",
                listOf("y"),
                LiteralCollectionNode(
                    listOf(
                        FunctionCallNode("x", FunctionType.Function, emptyList(), Location.NoProvided),
                        FunctionCallNode("y", FunctionType.Function, emptyList(), Location.NoProvided),
                    ), LiteralCollectionType.Tuple, Location.NoProvided
                ),
                Location.NoProvided
            )
        ).checkFunctionSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                TypeVisibilityTableBuilder(ErrorCollector()).build(),
                ts
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        "n",
                        ApplicationNode(
                            ApplicationName("::prelude", "tupleOf"),
                            listOf(
                                VarNode(
                                    "x",
                                    TypeSemanticInfo(
                                        typeParameterForTesting(2)
                                    ),
                                    Location.NoProvided
                                ),
                                VarNode(
                                    "y",
                                    Symbol(
                                        "y", typeParameterForTesting(0)
                                    ),
                                    Location.NoProvided
                                )
                            ),
                            TypeSemanticInfo(
                                typeParameterForTesting(3)
                            ),
                            Location.NoProvided
                        ),
                        EmptySemanticInfo,
                        Location.NoProvided
                    )
                )
            )
        }
    }
    should("Semantic node: map literal") {
        module(
            FunctionNode(
                true,
                "n",
                listOf(),
                LiteralCollectionNode(
                    listOf(
                        LiteralMapEntryNode(
                            LiteralValueNode("\"key1\"", LiteralValueType.String, Location.NoProvided),
                            LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided
                        ),
                        LiteralMapEntryNode(
                            LiteralValueNode("\"key2\"", LiteralValueType.String, Location.NoProvided),
                            LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided
                        )
                    ), LiteralCollectionType.Map, Location.NoProvided
                ),
                Location.NoProvided
            )
        ).checkFunctionSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                TypeVisibilityTableBuilder(ErrorCollector()).build(),
                ts
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        "n",
                        ApplicationNode(
                            ApplicationName("::prelude", "mapOf"),
                            listOf(
                                ApplicationNode(
                                    ApplicationName("::prelude", "pair"),
                                    listOf(
                                        ConstantNode("key1", TypeSemanticInfo(strTypePromise), Location.NoProvided),
                                        ConstantNode(1.toLong(), TypeSemanticInfo(byteTypePromise), Location.NoProvided)
                                    ),
                                    TypeSemanticInfo(
                                        typeParameterForTesting(1)
                                    ),
                                    Location.NoProvided
                                ),
                                ApplicationNode(
                                    ApplicationName("::prelude", "pair"),
                                    listOf(
                                        ConstantNode("key2", TypeSemanticInfo(strTypePromise), Location.NoProvided),
                                        ConstantNode(2.toLong(), TypeSemanticInfo(byteTypePromise), Location.NoProvided)
                                    ),
                                    TypeSemanticInfo(
                                        typeParameterForTesting(2)
                                    ),
                                    Location.NoProvided
                                )
                            ),
                            TypeSemanticInfo(
                                typeParameterForTesting(3)
                            ),
                            Location.NoProvided
                        ),
                        EmptySemanticInfo,
                        Location.NoProvided
                    )
                )
            )
        }
    }
    should("Semantic node: let") {
        module(
            FunctionNode(
                true,
                "n",
                listOf(),
                LetExpressionNode(
                    listOf(
                        MatchAssignNode(
                            MatchValueNode(
                                MatchValueType.Expression,
                                FunctionCallNode("x", FunctionType.Function, listOf(), Location.NoProvided),
                                Location.NoProvided
                            ),
                            FunctionCallNode(
                                "sum", FunctionType.Function, listOf(
                                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided)
                                ), Location.NoProvided
                            ),
                            Location.NoProvided
                        ),
                        MatchAssignNode(
                            MatchValueNode(
                                MatchValueType.Expression,
                                FunctionCallNode("y", FunctionType.Function, listOf(), Location.NoProvided),
                                Location.NoProvided
                            ),
                            LiteralValueNode("20", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided
                        )
                    ),
                    OperatorNode(
                        "+",
                        FunctionCallNode("x", FunctionType.Function, listOf(), Location.NoProvided),
                        FunctionCallNode("y", FunctionType.Function, listOf(), Location.NoProvided),
                        Location.NoProvided
                    ),
                    Location.NoProvided
                ),
                Location.NoProvided
            )
        ).checkFunctionSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                TypeVisibilityTableBuilder(ErrorCollector()).build(),
                ts
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        "n",
                        LetNode(
                            listOf(
                                LetBindingNode(
                                    VarNode(
                                        "x",
                                        Symbol("x", typeParameterForTesting(1)),
                                        Location.NoProvided
                                    ),
                                    ApplicationNode(
                                        ApplicationName(name = "sum"),
                                        listOf(
                                            ConstantNode(
                                                10.toLong(),
                                                TypeSemanticInfo(byteTypePromise),
                                                Location.NoProvided
                                            )
                                        ),
                                        TypeSemanticInfo(
                                            typeParameterForTesting(2)
                                        ),
                                        Location.NoProvided
                                    ),
                                    EmptySemanticInfo,
                                    Location.NoProvided
                                ),
                                LetBindingNode(
                                    VarNode(
                                        "y",
                                        Symbol("y", typeParameterForTesting(3)),
                                        Location.NoProvided
                                    ),
                                    ConstantNode(
                                        20.toLong(),
                                        TypeSemanticInfo(byteTypePromise),
                                        Location.NoProvided
                                    ),
                                    EmptySemanticInfo,
                                    Location.NoProvided
                                )
                            ),
                            ApplicationNode(
                                ApplicationName(name = "(+)"),
                                listOf(
                                    VarNode(
                                        "x",
                                        Symbol("x", typeParameterForTesting(1)),
                                        Location.NoProvided
                                    ),
                                    VarNode(
                                        "y",
                                        Symbol("y", typeParameterForTesting(3)),
                                        Location.NoProvided
                                    )
                                ),
                                TypeSemanticInfo(
                                    typeParameterForTesting(4)
                                ),
                                Location.NoProvided
                            ),
                            EmptySemanticInfo,
                            Location.NoProvided
                        ),
                        EmptySemanticInfo,
                        Location.NoProvided
                    )
                )
            )
        }
    }
}) {
    override suspend fun beforeAny(testCase: TestCase) {
        super.beforeAny(testCase)
        resetParameterCounterForTesting()
    }
}
