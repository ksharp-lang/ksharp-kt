package org.ksharp.semantics.expressions

import InferenceErrorCode
import io.kotest.core.Tuple4
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.ksharp.common.*
import org.ksharp.module.FunctionVisibility
import org.ksharp.module.prelude.preludeModule
import org.ksharp.nodes.*
import org.ksharp.nodes.FunctionType
import org.ksharp.nodes.semantic.*
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.nodes.*
import org.ksharp.semantics.scopes.Function
import org.ksharp.semantics.scopes.FunctionTable
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.PartialTypeSystem
import org.ksharp.typesystem.annotations.Annotation
import org.ksharp.typesystem.typeSystem
import org.ksharp.typesystem.types.*

private data class FunctionTableResult(
    val errors: List<Error>,
    val functionTable: FunctionTable
)

private fun typeParameterForTesting(id: Int) = TypeSemanticInfo(Either.Right(newParameterForTesting(id)))

private fun module(vararg functions: FunctionNode) =
    ModuleNode(
        "module", listOf(), listOf(), listOf(), listOf(*functions), Location.NoProvided
    )


private fun ModuleNode.buildFunctionTable(moduleTypeSystemInfo: ModuleTypeSystemInfo): FunctionTableResult {
    val errors = ErrorCollector()
    val (functionTable, _) = buildFunctionTable(errors, moduleTypeSystemInfo.typeSystem)
    return FunctionTableResult(
        errors.build(),
        functionTable
    )
}

class FunctionNodeSemanticFunctionTableTest : StringSpec({
    "table: function without declaration" {
        module(
            FunctionNode(
                false,
                true,
                null,
                "sum",
                listOf("a", "b"),
                OperatorNode(
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
                            null,
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
            alias(TypeVisibility.Internal, "Decl__sum") {
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
                            null,
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
            alias(TypeVisibility.Internal, "Decl__sum", listOf(Annotation("native", mapOf()))) {
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
                            listOf(Annotation("native", mapOf())),
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
                            null,
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
            alias(TypeVisibility.Internal, "Decl__sum") {
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
            alias(TypeVisibility.Internal, "Decl__sum") {
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
    val longTypePromise = ts.getTypeSemanticInfo("Long")
    val byteTypePromise = ts.getTypeSemanticInfo("Byte")
    val shortTypePromise = ts.getTypeSemanticInfo("Short")
    val intTypePromise = ts.getTypeSemanticInfo("Int")
    val strTypePromise = ts.getTypeSemanticInfo("String")
    val unitTypePromise = ts.getTypeSemanticInfo("Unit")
    context("Semantic node: constant function") {
        listOf<Tuple4<LiteralValueType, TypePromise, String, Any>>(
            Tuple4(LiteralValueType.Integer, byteTypePromise, "10", 10.toLong()),
            Tuple4(LiteralValueType.BinaryInteger, byteTypePromise, "0b0001", 1.toLong()),
            Tuple4(LiteralValueType.HexInteger, shortTypePromise, "0xFF", 255.toLong()),
            Tuple4(LiteralValueType.Integer, intTypePromise, "500000", 500000.toLong()),
            Tuple4(LiteralValueType.Integer, longTypePromise, "5000000000", 5000000000),
            Tuple4(LiteralValueType.OctalInteger, byteTypePromise, "0o01", 1.toLong()),
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
                        ts
                    )
                ).apply {
                    errors.shouldBeEmpty()
                    abstractions.shouldBe(
                        listOf(
                            AbstractionNode(
                                false,
                                null,
                                "n", ConstantNode(
                                    expectedValue,
                                    expectedType.cast(),
                                    Location.NoProvided
                                ),
                                AbstractionSemanticInfo(
                                    FunctionVisibility.Public,
                                    listOf(),
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
                ts
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        false,
                        null,
                        "n",
                        ApplicationNode(
                            ApplicationName(name = "(**)"),
                            listOf(
                                ConstantNode(
                                    10.toLong(),
                                    byteTypePromise,
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    2.toLong(),
                                    byteTypePromise,
                                    Location.NoProvided
                                )
                            ),
                            typeParameterForTesting(1),
                            Location.NoProvided
                        ),
                        AbstractionSemanticInfo(
                            FunctionVisibility.Public,
                            listOf(),
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
                ts
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        false,
                        listOf(Annotation("native", mapOf())),
                        "n",
                        ApplicationNode(
                            ApplicationName(name = "(**)"),
                            listOf(
                                ConstantNode(
                                    10.toLong(),
                                    byteTypePromise,
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    2.toLong(),
                                    byteTypePromise,
                                    Location.NoProvided
                                )
                            ),
                            typeParameterForTesting(1),
                            Location.NoProvided
                        ),
                        AbstractionSemanticInfo(
                            FunctionVisibility.Public,
                            listOf(),
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
            alias(TypeVisibility.Public, "Decl__n", listOf(Annotation("native", mapOf()))) {
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
                nTs.value
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        false,
                        listOf(Annotation("native", mapOf())),
                        "n",
                        ApplicationNode(
                            ApplicationName(name = "(**)"),
                            listOf(
                                ConstantNode(
                                    10.toLong(),
                                    byteTypePromise,
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    2.toLong(),
                                    byteTypePromise,
                                    Location.NoProvided
                                )
                            ),
                            typeParameterForTesting(0),
                            Location.NoProvided
                        ),
                        AbstractionSemanticInfo(
                            FunctionVisibility.Public,
                            listOf(),
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
                ts
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        false,
                        null,
                        "n",
                        ApplicationNode(
                            ApplicationName(null, "if"),
                            listOf(
                                ApplicationNode(
                                    ApplicationName(name = "(>)"),
                                    listOf(
                                        ConstantNode(
                                            4.toLong(),
                                            byteTypePromise,
                                            Location.NoProvided
                                        ),
                                        VarNode(
                                            "a",
                                            Symbol("a", typeParameterForTesting(0)),
                                            Location.NoProvided
                                        )
                                    ),
                                    typeParameterForTesting(2),
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    10.toLong(),
                                    byteTypePromise,
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    20.toLong(),
                                    byteTypePromise,
                                    Location.NoProvided
                                ),
                            ),
                            typeParameterForTesting(3),
                            Location.NoProvided
                        ),
                        AbstractionSemanticInfo(
                            FunctionVisibility.Public,
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
                ts
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        false,
                        null,
                        "n",
                        ApplicationNode(
                            ApplicationName(null, "if"),
                            listOf(
                                ApplicationNode(
                                    ApplicationName(name = "(>)"),
                                    listOf(
                                        ConstantNode(
                                            4.toLong(),
                                            byteTypePromise,
                                            Location.NoProvided
                                        ),
                                        VarNode(
                                            "a",
                                            Symbol("a", typeParameterForTesting(0)),
                                            Location.NoProvided
                                        )
                                    ),
                                    typeParameterForTesting(2),
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    10.toLong(),
                                    byteTypePromise,
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    Unit,
                                    (unitTypePromise),
                                    Location.NoProvided
                                ),
                            ),
                            (typeParameterForTesting(3)),
                            Location.NoProvided
                        ),
                        AbstractionSemanticInfo(
                            FunctionVisibility.Internal,
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
                ts
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        false,
                        null,
                        "n",
                        ApplicationNode(
                            ApplicationName("::prelude", "listOf"),
                            listOf(
                                ConstantNode(
                                    10.toLong(),
                                    byteTypePromise,
                                    Location.NoProvided
                                ),
                                ApplicationNode(
                                    ApplicationName(name = "(+)"),
                                    listOf(
                                        ConstantNode(
                                            2.toLong(),
                                            byteTypePromise,
                                            Location.NoProvided
                                        ),
                                        ConstantNode(
                                            1.toLong(),
                                            byteTypePromise,
                                            Location.NoProvided
                                        )
                                    ),
                                    (typeParameterForTesting(1)),
                                    Location.NoProvided
                                )
                            ),
                            typeParameterForTesting(2),
                            Location.NoProvided
                        ),
                        AbstractionSemanticInfo(
                            FunctionVisibility.Public, listOf(), TypeSemanticInfo(
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
                ts
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        false,
                        null,
                        "n",
                        ApplicationNode(
                            ApplicationName("::prelude", "setOf"),
                            listOf(
                                ConstantNode(
                                    1.toLong(),
                                    byteTypePromise,
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    2.toLong(),
                                    byteTypePromise,
                                    Location.NoProvided
                                ),
                                ConstantNode(
                                    3.toLong(),
                                    byteTypePromise,
                                    Location.NoProvided
                                )
                            ),
                            typeParameterForTesting(1),
                            Location.NoProvided
                        ),
                        AbstractionSemanticInfo(
                            FunctionVisibility.Public, listOf(), TypeSemanticInfo(
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
                ts
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        false,
                        null,
                        "n",
                        ApplicationNode(
                            ApplicationName("::prelude", "tupleOf"),
                            listOf(
                                ApplicationNode(
                                    ApplicationName(null, "x"),
                                    listOf(
                                        ConstantNode(
                                            kotlin.Unit,
                                            unitTypePromise,
                                            Location.NoProvided
                                        )
                                    ),
                                    typeParameterForTesting(3),
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
                            typeParameterForTesting(4),
                            Location.NoProvided
                        ),
                        AbstractionSemanticInfo(
                            FunctionVisibility.Public,
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
                ts
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        false,
                        null,
                        "n",
                        ApplicationNode(
                            ApplicationName("::prelude", "mapOf"),
                            listOf(
                                ApplicationNode(
                                    ApplicationName(null, "pair"),
                                    listOf(
                                        ConstantNode("key1", strTypePromise, Location.NoProvided),
                                        ConstantNode(1.toLong(), byteTypePromise, Location.NoProvided)
                                    ),
                                    typeParameterForTesting(1),
                                    Location.NoProvided
                                ),
                                ApplicationNode(
                                    ApplicationName(null, "pair"),
                                    listOf(
                                        ConstantNode("key2", strTypePromise, Location.NoProvided),
                                        ConstantNode(2.toLong(), byteTypePromise, Location.NoProvided)
                                    ),
                                    typeParameterForTesting(2),
                                    Location.NoProvided
                                )
                            ),
                            typeParameterForTesting(3),
                            Location.NoProvided
                        ),
                        AbstractionSemanticInfo(
                            FunctionVisibility.Public, listOf(), TypeSemanticInfo(
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
                            Location.NoProvided,
                            MatchAssignNodeLocations(Location.NoProvided)
                        ),
                        MatchAssignNode(
                            MatchValueNode(
                                MatchValueType.Expression,
                                FunctionCallNode("y", FunctionType.Function, listOf(), Location.NoProvided),
                                Location.NoProvided
                            ),
                            LiteralValueNode("20", LiteralValueType.Integer, Location.NoProvided),
                            Location.NoProvided,
                            MatchAssignNodeLocations(Location.NoProvided)
                        )
                    ),
                    OperatorNode(
                        "+",
                        FunctionCallNode("x", FunctionType.Function, listOf(), Location.NoProvided),
                        FunctionCallNode("y", FunctionType.Function, listOf(), Location.NoProvided),
                        Location.NoProvided,
                    ),
                    Location.NoProvided,
                    LetExpressionNodeLocations(Location.NoProvided)
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
                ts
            )
        ).apply {
            errors.shouldBeEmpty()
            abstractions.shouldBe(
                listOf(
                    AbstractionNode(
                        false,
                        null,
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
                                                byteTypePromise,
                                                Location.NoProvided
                                            )
                                        ),
                                        typeParameterForTesting(2),
                                        Location.NoProvided
                                    ),
                                    EmptySemanticInfo(),
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
                                        byteTypePromise,
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
                                        Symbol("y", typeParameterForTesting(3)),
                                        Location.NoProvided
                                    )
                                ),
                                typeParameterForTesting(4),
                                Location.NoProvided
                            ),
                            EmptySemanticInfo(),
                            Location.NoProvided
                        ),
                        AbstractionSemanticInfo(
                            FunctionVisibility.Public, listOf(), TypeSemanticInfo(
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
                    false,
                    null,
                    "ten", ConstantNode(
                        10.toLong(),
                        longTypePromise,
                        Location.NoProvided
                    ),
                    AbstractionSemanticInfo(FunctionVisibility.Public, listOf()),
                    Location.NoProvided
                )
            )
        )
        info.checkInferenceSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                ts
            ), preludeModule
        ).apply {
            this.shouldBe(info)
            this.abstractions.first()
                .info.getInferredType(Location.NoProvided)
                .shouldBeRight(listOf(ts["Unit"].valueOrNull!!, ts["Long"].valueOrNull!!).toFunctionType())
        }
    }
    "Check inference - abstraction with arguments" {
        val errors = ErrorCollector()
        val param = newParameter()
        val symbol = Symbol("a", TypeSemanticInfo(Either.Right(param)))
        val info = ModuleFunctionInfo(
            errors.build(),
            listOf(
                AbstractionNode(
                    false,
                    null,
                    "n",
                    ApplicationNode(
                        ApplicationName(null, "if"),
                        listOf(
                            ApplicationNode(
                                ApplicationName(name = "True"),
                                listOf(),
                                typeParameterForTesting(2),
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
                        typeParameterForTesting(3),
                        Location.NoProvided
                    ),
                    AbstractionSemanticInfo(
                        FunctionVisibility.Public,
                        listOf(
                            symbol
                        )
                    ),
                    Location.NoProvided
                )
            )
        )
        info.checkInferenceSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                ts
            ), preludeModule
        ).apply {
            this.shouldBe(info)
            this.abstractions.first()
                .info.getInferredType(Location.NoProvided)
                .shouldBeRight(listOf(ts["Long"].valueOrNull!!, ts["Long"].valueOrNull!!).toFunctionType())
        }
    }
    "Check inference - abstraction with error" {
        val errors = ErrorCollector()
        val param = newParameter()
        val symbol = Symbol("a", TypeSemanticInfo(Either.Right(param)))
        val info = ModuleFunctionInfo(
            errors.build(),
            listOf(
                AbstractionNode(
                    false,
                    null,
                    "n",
                    ApplicationNode(
                        ApplicationName("::prelude", "no-function"),
                        listOf(
                            ApplicationNode(
                                ApplicationName(name = "True"),
                                listOf(),
                                typeParameterForTesting(2),
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
                        typeParameterForTesting(3),
                        Location.NoProvided
                    ),
                    AbstractionSemanticInfo(
                        FunctionVisibility.Public,
                        listOf(
                            symbol
                        )
                    ),
                    Location.NoProvided
                )
            )
        )
        info.checkInferenceSemantics(
            ModuleTypeSystemInfo(
                listOf(),
                ts
            ), preludeModule
        ).apply {
            this.abstractions.shouldBeEmpty()
            this.errors.shouldBe(
                listOf(
                    InferenceErrorCode.FunctionNotFound.new(
                        Location.NoProvided,
                        "function" to "no-function True (Num NativeLong) @0"
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
