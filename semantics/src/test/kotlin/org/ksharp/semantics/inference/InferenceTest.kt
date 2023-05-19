package org.ksharp.semantics.inference

import InferenceErrorCode
import inferType
import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Either
import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.common.new
import org.ksharp.module.FunctionVisibility
import org.ksharp.module.ModuleInfo
import org.ksharp.module.moduleFunctions
import org.ksharp.module.prelude.preludeModule
import org.ksharp.nodes.semantic.*
import org.ksharp.semantics.nodes.*
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.newParameter
import org.ksharp.typesystem.types.toFunctionType

private fun createInferenceInfo(typeSystem: TypeSystem): InferenceInfo {
    val a = newParameter()
    val intType = typeSystem["Int"].valueOrNull!!
    val testModule = ModuleInfo(
        listOf(),
        typeSystem = typeSystem,
        functions = moduleFunctions {
            add("(test+)", a, a, a)
            add("(test*)", intType, intType, intType)
        }
    )
    return InferenceInfo(preludeModule, testModule)
}

class InferenceTest : StringSpec({
    val ts = preludeModule.typeSystem
    val unitTypePromise = ts.getTypeSemanticInfo("Unit")

    "Inference type over constants" {
        val module = createInferenceInfo(ts)
        val longTypePromise = ts.getTypeSemanticInfo("Long")
        AbstractionNode(
            "ten", ConstantNode(
                10.toLong(),
                longTypePromise,
                Location.NoProvided
            ),
            AbstractionSemanticInfo(FunctionVisibility.Public, listOf()),
            Location.NoProvided
        ).inferType(module).apply {
            shouldBeRight(
                listOf(
                    unitTypePromise.type.valueOrNull!!,
                    longTypePromise.type.valueOrNull!!
                ).toFunctionType()
            )
        }
    }
    "Inference type over operators" {
        val module = createInferenceInfo(ts)
        val longTypePromise = ts.getTypeSemanticInfo("Long")
        AbstractionNode(
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
                TypeSemanticInfo(Either.Right(newParameter())),
                Location.NoProvided
            ),
            AbstractionSemanticInfo(FunctionVisibility.Public, listOf()),
            Location.NoProvided
        ).inferType(module).apply {
            shouldBeRight(
                listOf(
                    unitTypePromise.type.valueOrNull!!,
                    longTypePromise.type.valueOrNull!!
                ).toFunctionType()
            )
        }
    }
    "Inference type over operators with substitution" {
        val module = createInferenceInfo(ts)
        val longTypePromise = ts.getTypeSemanticInfo("Long")
        val intTypePromise = ts.getTypeSemanticInfo("Int")
        AbstractionNode(
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
                TypeSemanticInfo(Either.Right(newParameter())),
                Location.NoProvided
            ),
            AbstractionSemanticInfo(FunctionVisibility.Public, listOf()),
            Location.NoProvided
        ).inferType(module).apply {
            shouldBeRight(
                listOf(
                    unitTypePromise.type.valueOrNull!!,
                    longTypePromise.type.valueOrNull!!
                ).toFunctionType()
            )
        }
    }
    "Inference type over operators and variables with substitution" {
        val module = createInferenceInfo(ts)
        val longTypePromise = ts.getTypeSemanticInfo("Long")
        val variable = TypeSemanticInfo(Either.Right(newParameter()))
        val abstraction = AbstractionNode(
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
                TypeSemanticInfo(Either.Right(newParameter())),
                Location.NoProvided
            ),
            AbstractionSemanticInfo(
                FunctionVisibility.Public,
                listOf(variable)
            ),
            Location.NoProvided
        )
        abstraction.inferType(module).apply {
            shouldBeRight(
                listOf(
                    longTypePromise.type.valueOrNull!!,
                    longTypePromise.type.valueOrNull!!
                ).toFunctionType()
            )
        }
        abstraction.expression.cast<ApplicationNode<SemanticInfo>>()
            .arguments.first().info.getInferredType(Location.NoProvided)
            .shouldBeRight(longTypePromise.type.valueOrNull!!)
    }
    "Inference type over operators and variables with substitution with function that is not parametric" {
        val module = createInferenceInfo(ts)
        val intTypePromise = ts.getTypeSemanticInfo("Int")
        val variable = TypeSemanticInfo(Either.Right(newParameter()))
        val abstraction = AbstractionNode(
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
                TypeSemanticInfo(Either.Right(newParameter())),
                Location.NoProvided
            ),
            AbstractionSemanticInfo(
                FunctionVisibility.Public,
                listOf(variable)
            ),
            Location.NoProvided
        )
        abstraction.inferType(module).apply {
            shouldBeRight(
                listOf(
                    intTypePromise.type.valueOrNull!!,
                    intTypePromise.type.valueOrNull!!
                ).toFunctionType()
            )
        }
        abstraction.expression.cast<ApplicationNode<SemanticInfo>>()
            .arguments.first().info.getInferredType(Location.NoProvided)
            .shouldBeRight(intTypePromise.type.valueOrNull!!)
    }
    "Inference prelude if function" {
        val module = createInferenceInfo(ts)
        val intTypePromise = ts.getTypeSemanticInfo("Int")
        val abstraction = AbstractionNode(
            "n",
            ApplicationNode(
                ApplicationName("::prelude", "if"),
                listOf(
                    ApplicationNode(
                        ApplicationName(name = "True"),
                        listOf(),
                        TypeSemanticInfo(Either.Right(newParameter())),
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
                TypeSemanticInfo(Either.Right(newParameter())),
                Location.NoProvided
            ),
            AbstractionSemanticInfo(
                FunctionVisibility.Public,
                listOf()
            ),
            Location.NoProvided
        )
        abstraction.inferType(module).apply {
            shouldBeRight(
                listOf(
                    unitTypePromise.type.valueOrNull!!,
                    intTypePromise.type.valueOrNull!!
                ).toFunctionType()
            )
        }
    }
    "Inference let binding" {
        val module = createInferenceInfo(ts)
        val longTypePromise = ts.getTypeSemanticInfo("Long")
        val parameter = TypeSemanticInfo(Either.Right(newParameter()))
        val abstraction = AbstractionNode(
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
                            TypeSemanticInfo(Either.Right(newParameter())),
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
            AbstractionSemanticInfo(FunctionVisibility.Public, listOf()),
            Location.NoProvided
        )
        abstraction.inferType(module).apply {
            shouldBeRight(
                listOf(
                    unitTypePromise.type.valueOrNull!!,
                    longTypePromise.type.valueOrNull!!
                ).toFunctionType()
            )
        }
    }
    "Inference test function doesn't exists" {
        val module = createInferenceInfo(ts)
        val intTypePromise = ts.getTypeSemanticInfo("Int")
        AbstractionNode(
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
                TypeSemanticInfo(Either.Right(newParameter())),
                Location.NoProvided
            ),
            AbstractionSemanticInfo(FunctionVisibility.Public, listOf()),
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
        val parameter = TypeSemanticInfo(Either.Right(newParameter()))
        val abstraction = AbstractionNode(
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
                            TypeSemanticInfo(Either.Right(newParameter())),
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
            AbstractionSemanticInfo(FunctionVisibility.Public, listOf()),
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
})
