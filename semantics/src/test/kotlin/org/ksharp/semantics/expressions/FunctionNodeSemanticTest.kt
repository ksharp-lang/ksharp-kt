package org.ksharp.semantics.expressions

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.Location
import org.ksharp.nodes.*
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.inference.MaybePolymorphicTypePromise
import org.ksharp.semantics.inference.ResolvedTypePromise
import org.ksharp.semantics.prelude.types.preludeTypeSystem
import org.ksharp.semantics.typesystem.ModuleTypeSystemInfo
import org.ksharp.semantics.typesystem.TypeVisibilityTableBuilder
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
                preludeTypeSystem.value
            )
        ).apply {
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
        val typeSystem = typeSystem(preludeTypeSystem) {
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
        val typeSystem = preludeTypeSystem.value
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
})