package org.ksharp.semantics.expressions

import io.kotest.core.Tuple4
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.module.prelude.preludeModule
import org.ksharp.nodes.*
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.nodes.semantic.ApplicationNode
import org.ksharp.nodes.semantic.ConstantNode
import org.ksharp.nodes.semantic.VarNode
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.inference.MaybePolymorphicTypePromise
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
import org.ksharp.typesystem.types.Concrete
import org.ksharp.typesystem.types.alias
import org.ksharp.typesystem.types.functionType

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
                                MaybePolymorphicTypePromise("a"),
                                MaybePolymorphicTypePromise("b"),
                                MaybePolymorphicTypePromise("return"),
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
                                ResolvedTypePromise(Concrete("Int")),
                                ResolvedTypePromise(Concrete("Int")),
                                ResolvedTypePromise(Concrete("Int")),
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
                                MaybePolymorphicTypePromise("return")
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
})

class FunctionNodeSemanticTransformSemanticNodeTest : ShouldSpec({
    val ts = preludeModule.typeSystem
    val longTypePromise = ts.getTypePromise("Long")
    val strTypePromise = ts.getTypePromise("String")
    val unitTypePromise = ts.getTypePromise("Unit")
    context("Semantic node: constant function") {
        listOf<Tuple4<LiteralValueType, TypePromise, String, Any>>(
            Tuple4(LiteralValueType.Integer, longTypePromise, "10", 10.toLong()),
            Tuple4(LiteralValueType.BinaryInteger, longTypePromise, "0b0001", 1.toLong()),
            Tuple4(LiteralValueType.HexInteger, longTypePromise, "0xFF", 255.toLong()),
            Tuple4(LiteralValueType.OctalInteger, longTypePromise, "0o01", 1.toLong()),
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
                            "(**)",
                            listOf(
                                ConstantNode(
                                    10.toLong(),
                                    TypeSemanticInfo(longTypePromise),
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    2.toLong(),
                                    TypeSemanticInfo(longTypePromise),
                                    Location.NoProvided
                                )
                            ),
                            TypeSemanticInfo(MaybePolymorphicTypePromise("app-return")),
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
                            "if",
                            listOf(
                                ApplicationNode(
                                    "(>)",
                                    listOf(
                                        ConstantNode(
                                            4.toLong(),
                                            TypeSemanticInfo(longTypePromise),
                                            Location.NoProvided
                                        ),
                                        VarNode(
                                            "a",
                                            Symbol(MaybePolymorphicTypePromise("a")),
                                            Location.NoProvided
                                        )
                                    ),
                                    TypeSemanticInfo(MaybePolymorphicTypePromise("app-return")),
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    10.toLong(),
                                    TypeSemanticInfo(longTypePromise),
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    20.toLong(),
                                    TypeSemanticInfo(longTypePromise),
                                    Location.NoProvided
                                ),
                            ),
                            TypeSemanticInfo(MaybePolymorphicTypePromise("if-return")),
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
                            "if",
                            listOf(
                                ApplicationNode(
                                    "(>)",
                                    listOf(
                                        ConstantNode(
                                            4.toLong(),
                                            TypeSemanticInfo(longTypePromise),
                                            Location.NoProvided
                                        ),
                                        VarNode(
                                            "a",
                                            Symbol(MaybePolymorphicTypePromise("a")),
                                            Location.NoProvided
                                        )
                                    ),
                                    TypeSemanticInfo(MaybePolymorphicTypePromise("app-return")),
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    10.toLong(),
                                    TypeSemanticInfo(longTypePromise),
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    Unit,
                                    TypeSemanticInfo(unitTypePromise),
                                    Location.NoProvided
                                ),
                            ),
                            TypeSemanticInfo(MaybePolymorphicTypePromise("if-return")),
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
            println(abstractions)
        }
    }
})