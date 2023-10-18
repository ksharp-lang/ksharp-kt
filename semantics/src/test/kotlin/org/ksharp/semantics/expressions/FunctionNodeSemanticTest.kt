package org.ksharp.semantics.expressions

import io.kotest.core.Tuple4
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.*
import org.ksharp.module.prelude.preludeModule
import org.ksharp.nodes.*
import org.ksharp.nodes.FunctionType
import org.ksharp.nodes.semantic.*
import org.ksharp.semantics.context.TypeSystemSemanticContext
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.inference.InferenceErrorCode
import org.ksharp.semantics.nodes.*
import org.ksharp.semantics.scopes.Function
import org.ksharp.semantics.scopes.FunctionTable
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.PartialTypeSystem
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.typeSystem
import org.ksharp.typesystem.types.*

private data class FunctionTableResult(
    val errors: List<Error>,
    val functionTable: FunctionTable
)

private fun typeParameterForTesting(id: Int) = TypeSemanticInfo(Either.Right(newParameterForTesting(id)))

private fun module(vararg functions: FunctionNode) =
    ModuleNode(
        "module",
        listOf(),
        listOf(),
        emptyList(),
        emptyList(),
        listOf(),
        listOf(*functions),
        listOf(),
        Location.NoProvided
    )


private fun ModuleNode.buildFunctionTable(moduleTypeSystemInfo: ModuleTypeSystemInfo): FunctionTableResult {
    val errors = ErrorCollector()
    val (functionTable, _) = functions.buildFunctionTable(
        errors,
        TypeSystemSemanticContext(moduleTypeSystemInfo.typeSystem)
    )
    return FunctionTableResult(
        errors.build(),
        functionTable
    )
}


class FunctionNodeSemanticFunctionTableTest : StringSpec({
    "table: function with invalid name" {
        module(
            FunctionNode(
                false,
                true,
                null,
                "dot.sum",
                listOf("a", "b"),
                OperatorNode(
                    "Operator10",
                    "+",
                    FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                    FunctionCallNode("b", FunctionType.Function, listOf(), Location.NoProvided),
                    Location.NoProvided
                ),
                Location.NoProvided,
                FunctionNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).buildFunctionTable(
            ModuleTypeSystemInfo(
                listOf(),
                preludeModule.typeSystem,
                emptyList(),
                emptyMap(),
            )
        ).apply {
            errors.shouldBe(
                listOf(
                    FunctionSemanticsErrorCode.InvalidFunctionName.new(Location.NoProvided, "name" to "dot.sum")
                )
            )
            functionTable["dot.sum"]
                .shouldBeNull()
        }
    }
    "table: function without declaration" {
        module(
            FunctionNode(
                false,
                true,
                null,
                "sum",
                listOf("a", "b"),
                OperatorNode(
                    "Operator10",
                    "+",
                    FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                    FunctionCallNode("b", FunctionType.Function, listOf(), Location.NoProvided),
                    Location.NoProvided
                ),
                Location.NoProvided,
                FunctionNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).buildFunctionTable(
            ModuleTypeSystemInfo(
                listOf(),
                preludeModule.typeSystem, emptyList(), emptyMap()
            )
        ).apply {
            errors.shouldBeEmpty()
            functionTable["sum/3"]
                .shouldNotBeNull()
                .apply {
                    first.shouldBe(
                        Function(
                            setOf(CommonAttribute.Public),
                            "sum",
                            listOf(
                                TypeSemanticInfo(Either.Right(newParameterForTesting(0))),
                                TypeSemanticInfo(Either.Right(newParameterForTesting(1))),
                                TypeSemanticInfo(Either.Right(newParameterForTesting(2))),
                            )
                        )
                    )
                    second.shouldBe(Location.NoProvided)
                }
        }
    }
    "table: function with declaration" {
        val typeSystem = typeSystem(PartialTypeSystem(preludeModule.typeSystem, listOf())) {
            type(setOf(CommonAttribute.Internal), "Decl__sum/3") {
                functionType {
                    type("Int")
                    type("Int")
                    type("Int")
                }
            }
        }.value
        module(
            FunctionNode(
                false,
                true,
                null,
                "sum",
                listOf("a", "b"),
                OperatorNode(
                    "Operator10",
                    "+",
                    FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                    FunctionCallNode("b", FunctionType.Function, listOf(), Location.NoProvided),
                    Location.NoProvided
                ),
                Location.NoProvided, FunctionNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).buildFunctionTable(
            ModuleTypeSystemInfo(
                listOf(),
                typeSystem, emptyList(), emptyMap()
            )
        ).apply {
            errors.shouldBeEmpty()
            functionTable["sum/3"]
                .shouldNotBeNull()
                .apply {
                    first.shouldBe(
                        Function(
                            setOf(CommonAttribute.Public),
                            "sum",
                            listOf(
                                TypeSemanticInfo(typeSystem.alias("Int")),
                                TypeSemanticInfo(typeSystem.alias("Int")),
                                TypeSemanticInfo(typeSystem.alias("Int")),
                            )
                        )
                    )
                    second.shouldBe(Location.NoProvided)
                }
        }
    }
    "table: function with declaration 2" {
        val typeSystem = typeSystem(PartialTypeSystem(preludeModule.typeSystem, listOf())) {
            type(setOf(CommonAttribute.Internal), "Decl__sum/3") {
                functionType {
                    type("Int")
                    type("Int")
                    type("Int")
                }
            }
        }.value
        module(
            FunctionNode(
                false,
                true,
                null,
                "sum",
                listOf("a", "b"),
                OperatorNode(
                    "Operator10",
                    "+",
                    FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                    FunctionCallNode("b", FunctionType.Function, listOf(), Location.NoProvided),
                    Location.NoProvided
                ),
                Location.NoProvided, FunctionNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).buildFunctionTable(
            ModuleTypeSystemInfo(
                listOf(),
                typeSystem, emptyList(), emptyMap()
            )
        ).apply {
            errors.shouldBeEmpty()
            functionTable["sum/3"]
                .shouldNotBeNull()
                .apply {
                    first.shouldBe(
                        Function(
                            setOf(CommonAttribute.Public),
                            "sum",
                            listOf(
                                TypeSemanticInfo(typeSystem.alias("Int")),
                                TypeSemanticInfo(typeSystem.alias("Int")),
                                TypeSemanticInfo(typeSystem.alias("Int")),
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
                false,
                true,
                null,
                "ten",
                listOf(),
                LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                Location.NoProvided, FunctionNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).buildFunctionTable(
            ModuleTypeSystemInfo(
                listOf(),
                typeSystem, emptyList(), emptyMap()
            )
        ).apply {
            errors.shouldBeEmpty()
            functionTable["ten/1"]
                .shouldNotBeNull()
                .apply {
                    first.shouldBe(
                        Function(
                            setOf(CommonAttribute.Public),
                            "ten",
                            listOf(
                                TypeSemanticInfo(typeSystem["Unit"]),
                                TypeSemanticInfo(Either.Right(newParameterForTesting(0))),
                            )
                        )
                    )
                    second.shouldBe(Location.NoProvided)
                }
        }
    }
    "table: function with declaration mismatch" {
        val typeSystem = typeSystem(PartialTypeSystem(preludeModule.typeSystem, listOf())) {
            type(setOf(CommonAttribute.Internal), "Decl__sum/2") {
                functionType {
                    type("Int")
                    type("Int")
                }
            }
        }.value
        module(
            FunctionNode(
                false,
                true,
                null,
                "sum",
                listOf("a", "b"),
                OperatorNode(
                    "Operator10",
                    "+",
                    FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                    FunctionCallNode("b", FunctionType.Function, listOf(), Location.NoProvided),
                    Location.NoProvided
                ),
                Location.NoProvided, FunctionNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).buildFunctionTable(
            ModuleTypeSystemInfo(
                listOf(),
                typeSystem, emptyList(), emptyMap()
            )
        ).apply {
            errors.shouldBeEmpty()
            functionTable["sum/3"]
                .shouldNotBeNull()
                .apply {
                    first.shouldBe(
                        Function(
                            setOf(CommonAttribute.Public),
                            "sum",
                            listOf(
                                TypeSemanticInfo(Either.Right(newParameterForTesting(0))),
                                TypeSemanticInfo(Either.Right(newParameterForTesting(1))),
                                TypeSemanticInfo(Either.Right(newParameterForTesting(2))),
                            )
                        )
                    )
                    second.shouldBe(Location.NoProvided)
                }
        }
    }
    "table: function with declaration mismatch 2" {
        val typeSystem = typeSystem(PartialTypeSystem(preludeModule.typeSystem, listOf())) {
            type(setOf(CommonAttribute.Internal), "Decl__sum/2") {
                functionType {
                    type("Int")
                    type("Int")
                }
            }
        }.value
        module(
            FunctionNode(
                false,
                true,
                null,
                "sum",
                listOf(),
                OperatorNode(
                    "Operator10",
                    "+",
                    FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                    FunctionCallNode("b", FunctionType.Function, listOf(), Location.NoProvided),
                    Location.NoProvided
                ),
                Location.NoProvided, FunctionNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).buildFunctionTable(
            ModuleTypeSystemInfo(
                listOf(),
                typeSystem, emptyList(), emptyMap()
            )
        ).apply {
            errors.shouldBeEmpty()
            functionTable["sum/1"]
                .shouldNotBeNull()
                .apply {
                    first.shouldBe(
                        Function(
                            setOf(CommonAttribute.Public),
                            "sum",
                            listOf(
                                TypeSemanticInfo(typeSystem["Unit"]),
                                TypeSemanticInfo(Either.Right(newParameterForTesting(0))),
                            )
                        )
                    )
                    second.shouldBe(Location.NoProvided)
                }
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
    val longTypePromise = ts.getTypeSemanticInfo("Long")
    val strTypePromise = ts.getTypeSemanticInfo("String")
    val unitTypePromise = ts.getTypeSemanticInfo("Unit")
    context("Semantic node: constant function") {
        listOf<Tuple4<LiteralValueType, TypePromise, String, Any>>(
            Tuple4(LiteralValueType.Integer, longTypePromise, "10", 10.toLong()),
            Tuple4(LiteralValueType.BinaryInteger, longTypePromise, "0b0001", 1.toLong()),
            Tuple4(LiteralValueType.HexInteger, longTypePromise, "0xFF", 255.toLong()),
            Tuple4(LiteralValueType.Integer, longTypePromise, "500000", 500000.toLong()),
            Tuple4(LiteralValueType.Integer, longTypePromise, "5000000000", 5000000000),
            Tuple4(LiteralValueType.OctalInteger, longTypePromise, "0o01", 1.toLong()),
            Tuple4(LiteralValueType.Decimal, ts.getTypeSemanticInfo("Double"), "1.5", 1.5),
            Tuple4(LiteralValueType.String, strTypePromise, "\"Hello\"", "Hello"),
            Tuple4(LiteralValueType.MultiLineString, strTypePromise, "\"\"\"Hello\nWorld\"\"\"", "Hello\nWorld"),
            Tuple4(LiteralValueType.Character, ts.getTypeSemanticInfo("Char"), "'c'", 'c'),
        ).forEach { (literalType, expectedType, value, expectedValue) ->
            should("value type $literalType") {
                module(
                    FunctionNode(
                        false,
                        true,
                        null,
                        "n",
                        listOf(),
                        LiteralValueNode(value, literalType, Location.NoProvided),
                        Location.NoProvided,
                        FunctionNodeLocations(
                            Location.NoProvided,
                            Location.NoProvided,
                            Location.NoProvided,
                            listOf(),
                            Location.NoProvided
                        )
                    )
                ).checkFunctionSemantics(
                    ModuleTypeSystemInfo(
                        listOf(),
                        ts, emptyList(), emptyMap()
                    )
                ).apply {
                    errors.shouldBeEmpty()
                    abstractions.shouldBe(
                        listOf(
                            AbstractionNode(
                                setOf(CommonAttribute.Public),
                                "n", ConstantNode(
                                    expectedValue,
                                    expectedType.cast(),
                                    Location.NoProvided
                                ),
                                AbstractionSemanticInfo(
                                    emptyList(),
                                    TypeSemanticInfo(Either.Right(newParameterForTesting(0)))
                                ),
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
                false,
                true,
                null,
                "n",
                listOf(),
                OperatorNode(
                    "Operator12",
                    "**",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided
                ),
                Location.NoProvided,
                FunctionNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkFunctionSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                ts, emptyList(), emptyMap()
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        setOf(CommonAttribute.Public),
                        "n",
                        ApplicationNode(
                            ApplicationName(name = "(**)"),
                            listOf(
                                ConstantNode(
                                    10.toLong(),
                                    longTypePromise,
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    2.toLong(),
                                    longTypePromise,
                                    Location.NoProvided
                                )
                            ),
                            ApplicationSemanticInfo(),
                            Location.NoProvided
                        ),
                        AbstractionSemanticInfo(
                            emptyList(),
                            TypeSemanticInfo(Either.Right(newParameterForTesting(0)))
                        ),
                        Location.NoProvided
                    )
                )
            )
        }
    }
    should("Semantic node: operator with annotation") {
        module(
            FunctionNode(
                false,
                true,
                listOf(
                    AnnotationNode(
                        "native", mapOf(), Location.NoProvided,
                        AnnotationNodeLocations(Location.NoProvided, Location.NoProvided, listOf())
                    )
                ),
                "n",
                listOf(),
                OperatorNode(
                    "Operator12",
                    "**",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided,
                ),
                Location.NoProvided,
                FunctionNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkFunctionSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                ts, emptyList(), emptyMap()
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        setOf(CommonAttribute.Public),
                        "n",
                        ApplicationNode(
                            ApplicationName(name = "(**)"),
                            listOf(
                                ConstantNode(
                                    10.toLong(),
                                    longTypePromise,
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    2.toLong(),
                                    longTypePromise,
                                    Location.NoProvided
                                )
                            ),
                            ApplicationSemanticInfo(),
                            Location.NoProvided
                        ),
                        AbstractionSemanticInfo(
                            emptyList(),
                            TypeSemanticInfo(Either.Right(newParameterForTesting(0)))
                        ),
                        Location.NoProvided
                    )
                )
            )
        }
    }
    should("Semantic node: operator with function declaration") {
        var fnType: ErrorOrType? = null
        val nTs = typeSystem(PartialTypeSystem(ts, emptyList())) {
            type(setOf(CommonAttribute.Public), "Decl__n/1") {
                functionType {
                    type("Unit")
                    type("Long")
                }.also {
                    fnType = it.flatMap {
                        Either.Right(it.arguments.last())
                    }
                }
            }
        }
        module(
            FunctionNode(
                false,
                true,
                null,
                "n",
                listOf(),
                OperatorNode(
                    "Operator12",
                    "**",
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided,
                ),
                Location.NoProvided,
                FunctionNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkFunctionSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                nTs.value, emptyList(), emptyMap()
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        setOf(CommonAttribute.Public),
                        "n",
                        ApplicationNode(
                            ApplicationName(name = "(**)"),
                            listOf(
                                ConstantNode(
                                    10.toLong(),
                                    longTypePromise,
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    2.toLong(),
                                    longTypePromise,
                                    Location.NoProvided
                                )
                            ),
                            ApplicationSemanticInfo(),
                            Location.NoProvided
                        ),
                        AbstractionSemanticInfo(
                            emptyList(),
                            TypeSemanticInfo(fnType!!)
                        ),
                        Location.NoProvided
                    )
                )
            )
        }
    }
    should("Semantic node: function with module name") {
        var fnType: ErrorOrType? = null
        val nTs = typeSystem(PartialTypeSystem(ts, emptyList())) {
            type(setOf(CommonAttribute.Public), "Decl__n/1") {
                functionType {
                    type("Unit")
                    type("Long")
                }.also {
                    fnType = it.flatMap {
                        Either.Right(it.arguments.last())
                    }
                }
            }
        }
        module(
            FunctionNode(
                false,
                true,
                null,
                "n",
                listOf(),
                FunctionCallNode(
                    "math.sum",
                    FunctionType.Function,
                    listOf(
                        LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                        LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                    ),
                    Location.NoProvided,
                ),
                Location.NoProvided,
                FunctionNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkFunctionSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                nTs.value, emptyList(), emptyMap()
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        setOf(CommonAttribute.Public),
                        "n",
                        ApplicationNode(
                            ApplicationName(pck = "math", name = "sum"),
                            listOf(
                                ConstantNode(
                                    10.toLong(),
                                    longTypePromise,
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    2.toLong(),
                                    longTypePromise,
                                    Location.NoProvided
                                )
                            ),
                            ApplicationSemanticInfo(),
                            Location.NoProvided
                        ),
                        AbstractionSemanticInfo(
                            emptyList(),
                            TypeSemanticInfo(fnType!!)
                        ),
                        Location.NoProvided
                    )
                )
            )
        }
    }
    should("Semantic node: if with else") {
        module(
            FunctionNode(
                false,
                true,
                null,
                "n",
                listOf("a"),
                IfNode(
                    OperatorNode(
                        "Operator8",
                        ">",
                        LiteralValueNode("4", LiteralValueType.Integer, Location.NoProvided),
                        FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                        Location.NoProvided
                    ),
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    LiteralValueNode("20", LiteralValueType.Integer, Location.NoProvided),
                    Location.NoProvided,
                    IfNodeLocations(Location.NoProvided, Location.NoProvided, Location.NoProvided)
                ),
                Location.NoProvided,
                FunctionNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkFunctionSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                ts, emptyList(), emptyMap()
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        setOf(CommonAttribute.Public),
                        "n",
                        ApplicationNode(
                            ApplicationName(null, "if"),
                            listOf(
                                ApplicationNode(
                                    ApplicationName(name = "(>)"),
                                    listOf(
                                        ConstantNode(
                                            4.toLong(),
                                            longTypePromise,
                                            Location.NoProvided
                                        ),
                                        VarNode(
                                            "a",
                                            Symbol("a", typeParameterForTesting(0)),
                                            Location.NoProvided
                                        )
                                    ),
                                    ApplicationSemanticInfo(),
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    10.toLong(),
                                    longTypePromise,
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    20.toLong(),
                                    longTypePromise,
                                    Location.NoProvided
                                ),
                            ),
                            ApplicationSemanticInfo(),
                            Location.NoProvided
                        ),
                        AbstractionSemanticInfo(
                            listOf(
                                Symbol(
                                    "a",
                                    TypeSemanticInfo(Either.Right(newParameterForTesting(0)))
                                )
                            ), TypeSemanticInfo(Either.Right(newParameterForTesting(1)))
                        ),
                        Location.NoProvided
                    )
                )
            )
        }
    }
    should("Semantic node: if without else") {
        module(
            FunctionNode(
                false,
                false,
                null,
                "n",
                listOf("a"),
                IfNode(
                    OperatorNode(
                        "Operator8",
                        ">",
                        LiteralValueNode("4", LiteralValueType.Integer, Location.NoProvided),
                        FunctionCallNode("a", FunctionType.Function, listOf(), Location.NoProvided),
                        Location.NoProvided,
                    ),
                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                    UnitNode(Location.NoProvided),
                    Location.NoProvided,
                    IfNodeLocations(Location.NoProvided, Location.NoProvided, Location.NoProvided)
                ),
                Location.NoProvided,
                FunctionNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkFunctionSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                ts, emptyList(), emptyMap()
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        setOf(CommonAttribute.Internal),
                        "n",
                        ApplicationNode(
                            ApplicationName(null, "if"),
                            listOf(
                                ApplicationNode(
                                    ApplicationName(name = "(>)"),
                                    listOf(
                                        ConstantNode(
                                            4.toLong(),
                                            longTypePromise,
                                            Location.NoProvided
                                        ),
                                        VarNode(
                                            "a",
                                            Symbol("a", typeParameterForTesting(0)),
                                            Location.NoProvided
                                        )
                                    ),
                                    ApplicationSemanticInfo(),
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    10.toLong(),
                                    longTypePromise,
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    Unit,
                                    (unitTypePromise),
                                    Location.NoProvided
                                ),
                            ),
                            ApplicationSemanticInfo(),
                            Location.NoProvided
                        ),
                        AbstractionSemanticInfo(
                            listOf(
                                Symbol(
                                    "a",
                                    TypeSemanticInfo(Either.Right(newParameterForTesting(0)))
                                )
                            ), TypeSemanticInfo(Either.Right(newParameterForTesting(1)))
                        ),
                        Location.NoProvided
                    )
                )
            )
        }
    }
    should("Semantic node: list literal") {
        module(
            FunctionNode(
                false,
                true,
                null,
                "n",
                listOf(),
                LiteralCollectionNode(
                    listOf(
                        LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided),
                        OperatorNode(
                            "Operator10",
                            "+",
                            LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                            LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided,
                        )
                    ),
                    LiteralCollectionType.List,
                    Location.NoProvided,
                ),
                Location.NoProvided,
                FunctionNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkFunctionSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                ts, emptyList(), emptyMap()
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        setOf(CommonAttribute.Public),
                        "n",
                        ApplicationNode(
                            ApplicationName(PRELUDE_COLLECTION_FLAG, "listOf"),
                            listOf(
                                ConstantNode(
                                    10.toLong(),
                                    longTypePromise,
                                    Location.NoProvided
                                ),
                                ApplicationNode(
                                    ApplicationName(name = "(+)"),
                                    listOf(
                                        ConstantNode(
                                            2.toLong(),
                                            longTypePromise,
                                            Location.NoProvided
                                        ),
                                        ConstantNode(
                                            1.toLong(),
                                            longTypePromise,
                                            Location.NoProvided
                                        )
                                    ),
                                    ApplicationSemanticInfo(),
                                    Location.NoProvided
                                )
                            ),
                            ApplicationSemanticInfo(),
                            Location.NoProvided
                        ),
                        AbstractionSemanticInfo(
                            emptyList(), TypeSemanticInfo(
                                Either.Right(
                                    newParameterForTesting(0)
                                )
                            )
                        ),
                        Location.NoProvided
                    )
                )
            )
        }
    }
    should("Semantic node: set literal") {
        module(
            FunctionNode(
                false,
                true,
                null,
                "n",
                listOf(),
                LiteralCollectionNode(
                    listOf(
                        LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                        LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                        LiteralValueNode("3", LiteralValueType.Integer, Location.NoProvided)
                    ),
                    LiteralCollectionType.Set,
                    Location.NoProvided,
                ),
                Location.NoProvided,
                FunctionNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkFunctionSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                ts, emptyList(), emptyMap()
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        setOf(CommonAttribute.Public),
                        "n",
                        ApplicationNode(
                            ApplicationName(PRELUDE_COLLECTION_FLAG, "setOf"),
                            listOf(
                                ConstantNode(
                                    1.toLong(),
                                    longTypePromise,
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    2.toLong(),
                                    longTypePromise,
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    3.toLong(),
                                    longTypePromise,
                                    Location.NoProvided
                                )
                            ),
                            ApplicationSemanticInfo(),
                            Location.NoProvided
                        ),
                        AbstractionSemanticInfo(
                            emptyList(), TypeSemanticInfo(
                                Either.Right(
                                    newParameterForTesting(0)
                                )
                            )
                        ),
                        Location.NoProvided
                    )
                )
            )
        }
    }
    should("Semantic node: tuple literal") {
        module(
            FunctionNode(
                false,
                true,
                null,
                "n",
                listOf("y"),
                LiteralCollectionNode(
                    listOf(
                        FunctionCallNode("x", FunctionType.Function, emptyList(), Location.NoProvided),
                        FunctionCallNode("y", FunctionType.Function, emptyList(), Location.NoProvided),
                    ),
                    LiteralCollectionType.Tuple, Location.NoProvided,
                ),
                Location.NoProvided,
                FunctionNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkFunctionSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                ts, emptyList(), emptyMap()
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        setOf(CommonAttribute.Public),
                        "n",
                        ApplicationNode(
                            ApplicationName(PRELUDE_COLLECTION_FLAG, "tupleOf"),
                            listOf(
                                ApplicationNode(
                                    ApplicationName(null, "x"),
                                    listOf(
                                        ConstantNode(
                                            Unit,
                                            unitTypePromise,
                                            Location.NoProvided
                                        )
                                    ),
                                    ApplicationSemanticInfo(),
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
                            ApplicationSemanticInfo(),
                            Location.NoProvided
                        ),
                        AbstractionSemanticInfo(
                            listOf(
                                Symbol(
                                    "y",
                                    TypeSemanticInfo(Either.Right(newParameterForTesting(0)))
                                )
                            ), TypeSemanticInfo(
                                Either.Right(
                                    newParameterForTesting(1)
                                )
                            )
                        ),
                        Location.NoProvided
                    )
                )
            )
        }
    }
    should("Semantic node: map literal") {
        module(
            FunctionNode(
                false,
                true,
                null,
                "n",
                listOf(),
                LiteralCollectionNode(
                    listOf(
                        LiteralMapEntryNode(
                            LiteralValueNode("\"key1\"", LiteralValueType.String, Location.NoProvided),
                            LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided,
                            LiteralMapEntryNodeLocations(Location.NoProvided)
                        ),
                        LiteralMapEntryNode(
                            LiteralValueNode("\"key2\"", LiteralValueType.String, Location.NoProvided),
                            LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided,
                            LiteralMapEntryNodeLocations(Location.NoProvided)
                        )
                    ),
                    LiteralCollectionType.Map, Location.NoProvided,
                ),
                Location.NoProvided,
                FunctionNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkFunctionSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                ts, emptyList(), emptyMap()
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        setOf(CommonAttribute.Public),
                        "n",
                        ApplicationNode(
                            ApplicationName(PRELUDE_COLLECTION_FLAG, "mapOf"),
                            listOf(
                                ApplicationNode(
                                    ApplicationName(null, "pair"),
                                    listOf(
                                        ConstantNode("key1", strTypePromise, Location.NoProvided),
                                        ConstantNode(1.toLong(), longTypePromise, Location.NoProvided)
                                    ),
                                    ApplicationSemanticInfo(),
                                    Location.NoProvided
                                ),
                                ApplicationNode(
                                    ApplicationName(null, "pair"),
                                    listOf(
                                        ConstantNode("key2", strTypePromise, Location.NoProvided),
                                        ConstantNode(2.toLong(), longTypePromise, Location.NoProvided)
                                    ),
                                    ApplicationSemanticInfo(),
                                    Location.NoProvided
                                )
                            ),
                            ApplicationSemanticInfo(),
                            Location.NoProvided
                        ),
                        AbstractionSemanticInfo(
                            emptyList(), TypeSemanticInfo(
                                Either.Right(
                                    newParameterForTesting(0)
                                )
                            )
                        ),
                        Location.NoProvided
                    )
                )
            )
        }
    }
    should("Semantic node: let") {
        module(
            FunctionNode(
                false,
                true,
                null,
                "n",
                listOf(),
                LetExpressionNode(
                    listOf(
                        MatchAssignNode(
                            FunctionCallNode("x", FunctionType.Function, listOf(), Location.NoProvided),
                            FunctionCallNode(
                                "sum", FunctionType.Function, listOf(
                                    LiteralValueNode("10", LiteralValueType.Integer, Location.NoProvided)
                                ), Location.NoProvided
                            ),
                            Location.NoProvided
                        ),
                        MatchAssignNode(
                            FunctionCallNode("y", FunctionType.Function, listOf(), Location.NoProvided),
                            LiteralValueNode("20", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided
                        )
                    ),
                    OperatorNode(
                        "Operator10",
                        "+",
                        FunctionCallNode("x", FunctionType.Function, listOf(), Location.NoProvided),
                        FunctionCallNode("y", FunctionType.Function, listOf(), Location.NoProvided),
                        Location.NoProvided,
                    ),
                    Location.NoProvided,
                    LetExpressionNodeLocations(Location.NoProvided, Location.NoProvided)
                ),
                Location.NoProvided, FunctionNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkFunctionSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                ts, emptyList(), emptyMap()
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        setOf(CommonAttribute.Public),
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
                                                longTypePromise,
                                                Location.NoProvided
                                            )
                                        ),
                                        ApplicationSemanticInfo(),
                                        Location.NoProvided
                                    ),
                                    EmptySemanticInfo(),
                                    Location.NoProvided
                                ),
                                LetBindingNode(
                                    VarNode(
                                        "y",
                                        Symbol("y", typeParameterForTesting(2)),
                                        Location.NoProvided
                                    ),
                                    ConstantNode(
                                        20.toLong(),
                                        longTypePromise,
                                        Location.NoProvided
                                    ),
                                    EmptySemanticInfo(),
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
                                        Symbol("y", typeParameterForTesting(2)),
                                        Location.NoProvided
                                    )
                                ),
                                ApplicationSemanticInfo(),
                                Location.NoProvided
                            ),
                            EmptySemanticInfo(),
                            Location.NoProvided
                        ),
                        AbstractionSemanticInfo(
                            emptyList(), TypeSemanticInfo(
                                Either.Right(
                                    newParameterForTesting(0)
                                )
                            )
                        ),
                        Location.NoProvided
                    )
                )
            )
        }
    }
    should("Semantic node: let with list match") {
        module(
            FunctionNode(
                false,
                true,
                null,
                "n",
                listOf(),
                LetExpressionNode(
                    matches = listOf(
                        MatchAssignNode(
                            MatchListValueNode(
                                head = listOf(
                                    FunctionCallNode(
                                        name = "x", type = FunctionType.Function, arguments = listOf(),
                                        location = Location.NoProvided
                                    )
                                ),
                                tail = LiteralValueNode(
                                    value = "y", type = LiteralValueType.Binding,
                                    location = Location.NoProvided
                                ),
                                location = Location.NoProvided,
                                locations = MatchListValueNodeLocations(tailSeparatorLocation = Location.NoProvided)
                            ),
                            expression = LiteralCollectionNode(
                                values = listOf(
                                    LiteralValueNode(
                                        value = "1",
                                        type = LiteralValueType.Integer,
                                        location = Location.NoProvided
                                    ),
                                    LiteralValueNode(
                                        value = "2", type = LiteralValueType.Integer,
                                        location = Location.NoProvided
                                    )
                                ), type = LiteralCollectionType.List, location = Location.NoProvided
                            ),
                            location = Location.NoProvided
                        )
                    ),
                    expression = OperatorNode(
                        "Operator10",
                        operator = "+",
                        left = FunctionCallNode(
                            name = "x",
                            type = FunctionType.Function,
                            arguments = listOf(),
                            location = Location.NoProvided
                        ),
                        right = FunctionCallNode(
                            name = "y",
                            type = FunctionType.Function,
                            arguments = listOf(),
                            location = Location.NoProvided
                        ),
                        location = Location.NoProvided
                    ),
                    location = Location.NoProvided,
                    locations = LetExpressionNodeLocations(
                        letLocation = Location.NoProvided,
                        thenLocation = Location.NoProvided
                    )
                ),
                Location.NoProvided, FunctionNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkFunctionSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                ts, emptyList(), emptyMap()
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        attributes = setOf(CommonAttribute.Public),
                        name = "n",
                        expression = LetNode(
                            bindings = listOf(
                                LetBindingNode(
                                    match = ListMatchValueNode(
                                        head = listOf(
                                            VarNode(
                                                name = "x",
                                                info = Symbol(name = "x", type = typeParameterForTesting(1)),
                                                location = Location.NoProvided
                                            )
                                        ),
                                        tail = VarNode(
                                            name = "y",
                                            info = Symbol(name = "y", type = typeParameterForTesting(2)),
                                            location = Location.NoProvided
                                        ),
                                        info = EmptySemanticInfo(),
                                        location = Location.NoProvided
                                    ),
                                    expression = ApplicationNode(
                                        functionName = ApplicationName(pck = "::collection", name = "listOf"),
                                        arguments = listOf(
                                            ConstantNode(
                                                value = 1.toLong(),
                                                info = longTypePromise, location = Location.NoProvided
                                            ),
                                            ConstantNode(
                                                value = 2.toLong(),
                                                info = longTypePromise,
                                                location = Location.NoProvided
                                            )
                                        ),
                                        info = ApplicationSemanticInfo(), location = Location.NoProvided
                                    ), info = EmptySemanticInfo(), location = Location.NoProvided
                                )
                            ),
                            expression = ApplicationNode(
                                functionName = ApplicationName(pck = null, name = "(+)"),
                                arguments = listOf(
                                    VarNode(
                                        name = "x",
                                        info = Symbol(name = "x", type = typeParameterForTesting(1)),
                                        location = Location.NoProvided
                                    ),
                                    VarNode(
                                        name = "y",
                                        info = Symbol(name = "y", type = typeParameterForTesting(2)),
                                        location = Location.NoProvided
                                    )
                                ),
                                info = ApplicationSemanticInfo(),
                                location = Location.NoProvided
                            ),
                            info = EmptySemanticInfo(),
                            location = Location.NoProvided
                        ),
                        info = AbstractionSemanticInfo(
                            emptyList(),
                            returnType = typeParameterForTesting(0)
                        ),
                        location = Location.NoProvided
                    )
                )
            )
        }
    }
    should("Semantic node: match expression") {
        module(
            FunctionNode(
                false,
                true,
                null,
                "n",
                listOf(),
                MatchExpressionNode(
                    LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                    listOf(
                        MatchExpressionBranchNode(
                            LiteralValueNode("1", LiteralValueType.Integer, Location.NoProvided),
                            LiteralValueNode("\"one\"", LiteralValueType.String, Location.NoProvided),
                            Location.NoProvided
                        ),
                        MatchExpressionBranchNode(
                            MatchConditionValueNode(
                                type = MatchConditionalType.Or,
                                left = LiteralValueNode("2", LiteralValueType.Integer, Location.NoProvided),
                                right = LiteralValueNode("3", LiteralValueType.Integer, Location.NoProvided),
                                Location.NoProvided
                            ),
                            LiteralValueNode("\"other\"", LiteralValueType.String, Location.NoProvided),
                            Location.NoProvided
                        )
                    ),
                    Location.NoProvided,
                    MatchExpressionNodeLocations(Location.NoProvided, Location.NoProvided)
                ),
                Location.NoProvided, FunctionNodeLocations(
                    Location.NoProvided,
                    Location.NoProvided,
                    Location.NoProvided,
                    listOf(),
                    Location.NoProvided
                )
            )
        ).checkFunctionSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                ts, emptyList(), emptyMap()
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        attributes = setOf(CommonAttribute.Public),
                        name = "n",
                        expression = MatchNode(
                            expression = ConstantNode(value = 1.toLong(), info = longTypePromise, Location.NoProvided),
                            branches = listOf(
                                MatchBranchNode(
                                    match = ConstantNode(
                                        value = 1.toLong(),
                                        info = longTypePromise,
                                        Location.NoProvided
                                    ),
                                    expression = ConstantNode(
                                        value = "one",
                                        info = strTypePromise,
                                        Location.NoProvided
                                    ),
                                    info = EmptySemanticInfo(), Location.NoProvided
                                ),
                                MatchBranchNode(
                                    match = ConditionalMatchValueNode(
                                        type = MatchConditionalType.Or,
                                        left = ConstantNode(
                                            value = 2.toLong(),
                                            info = longTypePromise,
                                            Location.NoProvided
                                        ),
                                        right = ConstantNode(
                                            value = 3.toLong(),
                                            info = longTypePromise,
                                            Location.NoProvided
                                        ),
                                        info = EmptySemanticInfo(), Location.NoProvided
                                    ),
                                    expression = ConstantNode(
                                        value = "other",
                                        info = strTypePromise,
                                        Location.NoProvided
                                    ),
                                    info = EmptySemanticInfo(), Location.NoProvided
                                )
                            ), info = EmptySemanticInfo(), Location.NoProvided
                        ),
                        info = AbstractionSemanticInfo(
                            emptyList(),
                            returnType = typeParameterForTesting(0)
                        ),
                        location = Location.NoProvided
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

class FunctionNodeSemanticCheckInferenceTest : StringSpec({
    val ts = preludeModule.typeSystem
    val longTypePromise = ts.getTypeSemanticInfo("Long")
    "Check inference" {
        val errors = ErrorCollector()
        val info = ModuleFunctionInfo(
            errors.build(),
            listOf(
                AbstractionNode(
                    setOf(CommonAttribute.Public),
                    "ten", ConstantNode(
                        10.toLong(),
                        longTypePromise,
                        Location.NoProvided
                    ),
                    AbstractionSemanticInfo(listOf()),
                    Location.NoProvided
                )
            ), emptyMap(), emptyMap()
        )
        info.checkInferenceSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                ts, emptyList(), emptyMap()
            ), preludeModule
        ).apply {
            this.shouldBe(info)
            this.abstractions.first()
                .info.getInferredType(Location.NoProvided)
                .shouldBeRight(
                    listOf(ts["Unit"].valueOrNull!!, ts["Long"].valueOrNull!!).toFunctionType(
                        MockHandlePromise()
                    )
                )
        }
    }
    "Check inference - abstraction with arguments" {
        val errors = ErrorCollector()
        val param = ts.newParameter()
        val symbol = Symbol("a", TypeSemanticInfo(Either.Right(param)))
        val info = ModuleFunctionInfo(
            errors.build(),
            listOf(
                AbstractionNode(
                    setOf(CommonAttribute.Public),
                    "n",
                    ApplicationNode(
                        ApplicationName(null, "if"),
                        listOf(
                            ApplicationNode(
                                ApplicationName(name = "True"),
                                listOf(),
                                ApplicationSemanticInfo(),
                                Location.NoProvided
                            ),
                            ConstantNode(
                                10.toLong(),
                                longTypePromise,
                                Location.NoProvided
                            ),
                            VarNode(
                                "a",
                                symbol,
                                Location.NoProvided
                            ),
                        ),
                        ApplicationSemanticInfo(),
                        Location.NoProvided
                    ),
                    AbstractionSemanticInfo(
                        listOf(
                            symbol
                        )
                    ),
                    Location.NoProvided
                )
            ), emptyMap(), emptyMap()
        )
        info.checkInferenceSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                ts, emptyList(), emptyMap()
            ), preludeModule.also {
                it.functions.keys.onEach { println(it) }
            }
        ).apply {
            this.shouldBe(info)
            this.abstractions.first()
                .info.getInferredType(Location.NoProvided)
                .shouldBeRight(
                    listOf(ts["Long"].valueOrNull!!, ts["Long"].valueOrNull!!).toFunctionType(
                        MockHandlePromise()
                    )
                )
        }
    }
    "Check inference - abstraction with error" {
        val errors = ErrorCollector()
        val param = ts.newParameter()
        val symbol = Symbol("a", TypeSemanticInfo(Either.Right(param)))
        val info = ModuleFunctionInfo(
            errors.build(),
            listOf(
                AbstractionNode(
                    NoAttributes,
                    "n",
                    ApplicationNode(
                        ApplicationName(null, "no-function"),
                        listOf(
                            ApplicationNode(
                                ApplicationName(name = "True"),
                                listOf(),
                                ApplicationSemanticInfo(),
                                Location.NoProvided
                            ),
                            ConstantNode(
                                10.toLong(),
                                longTypePromise,
                                Location.NoProvided
                            ),
                            VarNode(
                                "a",
                                symbol,
                                Location.NoProvided
                            ),
                        ),
                        ApplicationSemanticInfo(),
                        Location.NoProvided
                    ),
                    AbstractionSemanticInfo(
                        listOf(
                            symbol
                        )
                    ),
                    Location.NoProvided
                )
            ), emptyMap(), emptyMap()
        )
        info.checkInferenceSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                ts, emptyList(), emptyMap()
            ), preludeModule
        ).apply {
            this.abstractions.shouldBeEmpty()
            this.errors.shouldBe(
                listOf(
                    InferenceErrorCode.FunctionNotFound.new(
                        Location.NoProvided,
                        "function" to "no-function True (Num numeric<Long>) @0"
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
