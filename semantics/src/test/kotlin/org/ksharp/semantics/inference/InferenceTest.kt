package org.ksharp.semantics.inference

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.*
import org.ksharp.module.ModuleInfo
import org.ksharp.module.functionInfo
import org.ksharp.module.prelude.preludeModule
import org.ksharp.nodes.semantic.*
import org.ksharp.semantics.expressions.PRELUDE_COLLECTION_FLAG
import org.ksharp.semantics.nodes.*
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.attributes.NameAttribute
import org.ksharp.typesystem.types.newParameter
import org.ksharp.typesystem.types.newParameterForTesting
import org.ksharp.typesystem.types.toFunctionType

private fun createInferenceInfo(typeSystem: TypeSystem): InferenceInfo {
    val a = typeSystem.newParameter()
    val intType = typeSystem["Int"].valueOrNull!!
    val testModule = ModuleInfo(
        listOf(),
        typeSystem = typeSystem,
        functions = mapOf(
            "(test+)/3" to functionInfo(
                setOf(CommonAttribute.Public),
                "(test+)",
                listOf(a, a, a)
            ),
            "(test*)/3" to functionInfo(
                setOf(CommonAttribute.Public),
                "(test+)",
                listOf(intType, intType, intType)
            )
        ),
        emptyMap(),
        emptySet()
    )
    return InferenceInfo(ConcreteModuleInfo(preludeModule), ConcreteModuleInfo(testModule))
}

class InferenceTest : StringSpec({
    val ts = preludeModule.typeSystem
    val unitTypePromise = ts.getTypeSemanticInfo("Unit")

    "Inference type over constants" {
        val module = createInferenceInfo(ts)
        val longTypePromise = ts.getTypeSemanticInfo("Long")
        AbstractionNode(
            setOf(CommonAttribute.Public),
            "ten", ConstantNode(
                10.toLong(),
                longTypePromise,
                Location.NoProvided
            ),
            AbstractionSemanticInfo(listOf()),
            Location.NoProvided
        ).inferType(module).apply {
            shouldBeRight(
                listOf(
                    unitTypePromise.type.valueOrNull!!,
                    longTypePromise.type.valueOrNull!!
                ).toFunctionType(MockHandlePromise())
            )
        }
    }
    "Inference type over native abstraction" {
        val module = createInferenceInfo(ts)
        val longTypePromise = ts.getTypeSemanticInfo("Long")
        val param = newParameterForTesting(0)
        AbstractionNode(
            setOf(CommonAttribute.Public, CommonAttribute.Native),
            "ten", ConstantNode(
                10.toLong(),
                longTypePromise,
                Location.NoProvided
            ),
            AbstractionSemanticInfo(
                listOf(), TypeSemanticInfo(
                    Either.Right(
                        param
                    )
                )
            ),
            Location.NoProvided
        ).inferType(module).apply {
            shouldBeRight(
                listOf(
                    unitTypePromise.type.valueOrNull!!,
                    param
                ).toFunctionType(MockHandlePromise())
            )
        }
    }
    "Inference type over operators" {
        val module = createInferenceInfo(ts)
        val longTypePromise = ts.getTypeSemanticInfo("Long")
        AbstractionNode(
            setOf(CommonAttribute.Public),
            "n",
            ApplicationNode(
                ApplicationName(name = "(test+)"),
                listOf(
                    ConstantNode(
                        10.toLong(),
                        (longTypePromise),
                        Location.NoProvided
                    ),
                    ConstantNode(
                        2.toLong(),
                        (longTypePromise),
                        Location.NoProvided
                    )
                ),
                ApplicationSemanticInfo(),
                Location.NoProvided
            ),
            AbstractionSemanticInfo(listOf()),
            Location.NoProvided
        ).inferType(module).apply {
            map { it.representation }
                .shouldBeRight("(Unit -> (Num numeric<Long>))")
        }
    }
    "Inference type over operators with substitution" {
        val module = createInferenceInfo(ts)
        val longTypePromise = ts.getTypeSemanticInfo("Long")
        val intTypePromise = ts.getTypeSemanticInfo("Int")
        AbstractionNode(
            setOf(CommonAttribute.Public),
            "n",
            ApplicationNode(
                ApplicationName(name = "(test+)"),
                listOf(
                    ConstantNode(
                        10.toLong(),
                        (intTypePromise),
                        Location.NoProvided
                    ),
                    ConstantNode(
                        2.toLong(),
                        (longTypePromise),
                        Location.NoProvided
                    )
                ),
                ApplicationSemanticInfo(),
                Location.NoProvided
            ),
            AbstractionSemanticInfo(listOf()),
            Location.NoProvided
        ).inferType(module).apply {
            map { it.representation }
                .shouldBeRight("(Unit -> (Num numeric<Long>))")
        }
    }
    "Inference type over operators and variables with substitution" {
        val module = createInferenceInfo(ts)
        val longTypePromise = ts.getTypeSemanticInfo("Long")
        val variable = Symbol("x", TypeSemanticInfo(Either.Right(ts.newParameter())))
        val abstraction = AbstractionNode(
            setOf(CommonAttribute.Public),
            "n",
            ApplicationNode(
                ApplicationName(name = "(test+)"),
                listOf(
                    VarNode(
                        "x",
                        variable,
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
                listOf(variable)
            ),
            Location.NoProvided
        )
        abstraction.inferType(module).apply {
            map { it.representation }
                .shouldBeRight("((Num numeric<Long>) -> (Num numeric<Long>))")
        }
        abstraction.expression.cast<ApplicationNode<SemanticInfo>>()
            .arguments.first().info.getInferredType(Location.NoProvided)
            .shouldBeRight(longTypePromise.type.valueOrNull!!)
    }
    "Inference type over operators and variables with substitution with function that is not parametric" {
        val module = createInferenceInfo(ts)
        val intTypePromise = ts.getTypeSemanticInfo("Int")
        val variable = Symbol("x", TypeSemanticInfo(Either.Right(ts.newParameter())))
        val abstraction = AbstractionNode(
            setOf(CommonAttribute.Public),
            "n",
            ApplicationNode(
                ApplicationName(name = "(test*)"),
                listOf(
                    VarNode(
                        "x",
                        variable,
                        Location.NoProvided
                    ),
                    VarNode(
                        "x",
                        variable,
                        Location.NoProvided
                    ),
                ),
                ApplicationSemanticInfo(),
                Location.NoProvided
            ),
            AbstractionSemanticInfo(
                listOf(variable)
            ),
            Location.NoProvided
        )
        abstraction.inferType(module).apply {
            map { it.representation }
                .shouldBeRight("((Num numeric<Int>) -> (Num numeric<Int>))")
        }
        abstraction.expression.cast<ApplicationNode<SemanticInfo>>()
            .arguments.first().info.getInferredType(Location.NoProvided)
            .shouldBeRight(intTypePromise.type.valueOrNull!!)
    }
    "Inference prelude if function" {
        val module = createInferenceInfo(ts)
        val intTypePromise = ts.getTypeSemanticInfo("Int")
        val abstraction = AbstractionNode(
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
                        intTypePromise,
                        Location.NoProvided
                    ),
                    ConstantNode(
                        20.toLong(),
                        intTypePromise,
                        Location.NoProvided
                    ),
                ),
                ApplicationSemanticInfo(),
                Location.NoProvided
            ),
            AbstractionSemanticInfo(
                listOf()
            ),
            Location.NoProvided
        )
        abstraction.inferType(module).apply {
            map { it.representation }
                .shouldBeRight("(Unit -> (Num numeric<Int>))")
        }
    }
    "Inference let binding" {
        val module = createInferenceInfo(ts)
        val longTypePromise = ts.getTypeSemanticInfo("Long")
        val parameter = Symbol("x", TypeSemanticInfo(Either.Right(ts.newParameter())))
        val abstraction = AbstractionNode(
            setOf(CommonAttribute.Public),
            "n",
            LetNode(
                listOf(
                    LetBindingNode(
                        VarNode("x", parameter, Location.NoProvided),
                        ApplicationNode(
                            ApplicationName(name = "(test+)"),
                            listOf(
                                ConstantNode(
                                    2.toLong(),
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
                        EmptySemanticInfo(),
                        Location.NoProvided
                    )
                ),
                VarNode("x", parameter, Location.NoProvided),
                EmptySemanticInfo(),
                Location.NoProvided
            ),
            AbstractionSemanticInfo(listOf()),
            Location.NoProvided
        )
        abstraction.inferType(module).apply {
            map { it.representation }
                .shouldBeRight("(Unit -> (Num numeric<Long>))")
        }
    }
    "Inference test function doesn't exists" {
        val module = createInferenceInfo(ts)
        val intTypePromise = ts.getTypeSemanticInfo("Int")
        AbstractionNode(
            setOf(CommonAttribute.Public),
            "n",
            ApplicationNode(
                ApplicationName(name = "not-found"),
                listOf(
                    ConstantNode(
                        10.toLong(),
                        (intTypePromise),
                        Location.NoProvided
                    )
                ),
                ApplicationSemanticInfo(),
                Location.NoProvided
            ),
            AbstractionSemanticInfo(listOf()),
            Location.NoProvided
        ).inferType(module).apply {
            shouldBeLeft(
                InferenceErrorCode.FunctionNotFound.new(
                    Location.NoProvided,
                    "function" to "not-found (Num numeric<Int>)"
                )
            )
        }
    }
    "Inference let binding with binding error" {
        val module = createInferenceInfo(ts)
        val longTypePromise = ts.getTypeSemanticInfo("Long")
        val parameter = TypeSemanticInfo(Either.Right(ts.newParameter()))
        val abstraction = AbstractionNode(
            setOf(CommonAttribute.Public),
            "n",
            LetNode(
                listOf(
                    LetBindingNode(
                        VarNode("x", parameter, Location.NoProvided),
                        ApplicationNode(
                            ApplicationName(name = "not-found"),
                            listOf(
                                ConstantNode(
                                    2.toLong(),
                                    longTypePromise,
                                    Location.NoProvided
                                )
                            ),
                            ApplicationSemanticInfo(),
                            Location.NoProvided
                        ),
                        EmptySemanticInfo(),
                        Location.NoProvided
                    )
                ),
                VarNode("x", parameter, Location.NoProvided),
                EmptySemanticInfo(),
                Location.NoProvided
            ),
            AbstractionSemanticInfo(listOf()),
            Location.NoProvided
        )
        abstraction.inferType(module).apply {
            shouldBeLeft(
                InferenceErrorCode.FunctionNotFound.new(
                    Location.NoProvided,
                    "function" to "not-found (Num numeric<Long>)"
                )
            )
        }
    }
    "listOf inference" {
        val module = createInferenceInfo(ts)
        val byteTypePromise = ts.getTypeSemanticInfo("Byte")
        AbstractionNode(
            setOf(CommonAttribute.Public),
            "n",
            ApplicationNode(
                ApplicationName(PRELUDE_COLLECTION_FLAG, "listOf"),
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
                        ApplicationSemanticInfo(),
                        Location.NoProvided
                    )
                ),
                ApplicationSemanticInfo(),
                Location.NoProvided
            ),
            AbstractionSemanticInfo(
                listOf(), TypeSemanticInfo(
                    Either.Right(
                        newParameterForTesting(0)
                    )
                )
            ),
            Location.NoProvided
        ).inferType(module)
            .map { it.representation }
            .shouldBeRight("(Unit -> (List (Num numeric<Byte>)))")
    }
    "mapOf inference" {
        val module = createInferenceInfo(ts)
        val byteTypePromise = ts.getTypeSemanticInfo("Byte")
        val strTypePromise = ts.getTypeSemanticInfo("String")
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
                            ConstantNode(1.toLong(), byteTypePromise, Location.NoProvided)
                        ),
                        ApplicationSemanticInfo(),
                        Location.NoProvided
                    ),
                    ApplicationNode(
                        ApplicationName(null, "pair"),
                        listOf(
                            ConstantNode("key2", strTypePromise, Location.NoProvided),
                            ConstantNode(2.toLong(), byteTypePromise, Location.NoProvided)
                        ),
                        ApplicationSemanticInfo(),
                        Location.NoProvided
                    )
                ),
                ApplicationSemanticInfo(),
                Location.NoProvided
            ),
            AbstractionSemanticInfo(
                listOf(), TypeSemanticInfo(
                    Either.Right(
                        newParameterForTesting(0)
                    )
                )
            ),
            Location.NoProvided
        ).inferType(module)
            .map { it.representation }
            .shouldBeRight("(Unit -> (Map String (Num numeric<Byte>)))")
    }
    "setOf inference" {
        val module = createInferenceInfo(ts)
        val byteTypePromise = ts.getTypeSemanticInfo("Byte")
        AbstractionNode(
            setOf(CommonAttribute.Public),
            "n",
            ApplicationNode(
                ApplicationName(PRELUDE_COLLECTION_FLAG, "setOf"),
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
                        ApplicationSemanticInfo(),
                        Location.NoProvided
                    )
                ),
                ApplicationSemanticInfo(),
                Location.NoProvided
            ),
            AbstractionSemanticInfo(
                listOf(), TypeSemanticInfo(
                    Either.Right(
                        newParameterForTesting(0)
                    )
                )
            ),
            Location.NoProvided
        ).inferType(module)
            .map { it.representation }
            .shouldBeRight("(Unit -> (Set (Num numeric<Byte>)))")
    }
    "tupleOf inference" {
        val module = createInferenceInfo(ts)
        val byteTypePromise = ts.getTypeSemanticInfo("Byte")
        val abstraction = AbstractionNode(
            setOf(CommonAttribute.Public),
            "n",
            ApplicationNode(
                ApplicationName(PRELUDE_COLLECTION_FLAG, "tupleOf"),
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
                        ApplicationSemanticInfo(),
                        Location.NoProvided
                    )
                ),
                ApplicationSemanticInfo(),
                Location.NoProvided
            ),
            AbstractionSemanticInfo(
                listOf(), TypeSemanticInfo(
                    Either.Right(
                        newParameterForTesting(0)
                    )
                )
            ),
            Location.NoProvided
        )
        abstraction.inferType(module)
            .map { it.representation }
            .shouldBeRight("(Unit -> ((Num numeric<Byte>), (Num numeric<Byte>)))")
        abstraction.expression.info.cast<ApplicationSemanticInfo>()
            .function!!
            .attributes.filterIsInstance<NameAttribute>()
            .first().value["ir"]!!.shouldBe("prelude::tupleOf")
    }
})
