package org.ksharp.semantics.expressions

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.module.prelude.preludeModule
import org.ksharp.nodes.*
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.inference.MaybePolymorphicTypePromise
import org.ksharp.semantics.inference.ResolvedTypePromise
import org.ksharp.semantics.nodes.ModuleTypeSystemInfo
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
                                MaybePolymorphicTypePromise("a", "@param_1"),
                                MaybePolymorphicTypePromise("b", "@param_2"),
                                MaybePolymorphicTypePromise("return", "@param_3"),
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
                                MaybePolymorphicTypePromise("return", "@param_1")
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

class FunctionNodeSemanticTransformSemanticNodeTest : StringSpec({
    "table: constant function" {
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
                                MaybePolymorphicTypePromise("a", "@param_1"),
                                MaybePolymorphicTypePromise("b", "@param_2"),
                                MaybePolymorphicTypePromise("return", "@param_3"),
                            )
                        )
                    )
                    second.shouldBe(Location.NoProvided)
                }
        }
    }
})