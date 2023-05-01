package org.ksharp.semantics.inference

import inferType
import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Either
import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.module.ModuleInfo
import org.ksharp.module.moduleFunctions
import org.ksharp.module.prelude.preludeModule
import org.ksharp.nodes.semantic.*
import org.ksharp.semantics.nodes.EmptySemanticInfo
import org.ksharp.semantics.nodes.SemanticInfo
import org.ksharp.semantics.nodes.TypeSemanticInfo
import org.ksharp.semantics.nodes.getTypeSemanticInfo
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.Parameter
import org.ksharp.typesystem.types.newParameter
import org.ksharp.typesystem.types.toFunctionType

private fun createInferenceInfo(typeSystem: TypeSystem): InferenceInfo {
    val a = newParameter()
    val testModule = ModuleInfo(
        listOf(),
        typeSystem = typeSystem,
        functions = moduleFunctions {
            add("(+)", a, a, a)
        }
    )
    return InferenceInfo(preludeModule, testModule)
}

class InferenceTest : StringSpec({
    val ts = preludeModule.typeSystem

    "Inference type over constants" {
        val module = createInferenceInfo(ts)
        val longTypePromise = ts.getTypeSemanticInfo("Long")
        AbstractionNode(
            "ten", ConstantNode(
                10.toLong(),
                longTypePromise,
                Location.NoProvided
            ),
            EmptySemanticInfo(),
            Location.NoProvided
        ).inferType(module).apply {
            shouldBeRight(longTypePromise.type.valueOrNull!!)
        }
    }
    "Inference type over operators" {
        val module = createInferenceInfo(ts)
        val longTypePromise = ts.getTypeSemanticInfo("Long")
        AbstractionNode(
            "n",
            ApplicationNode(
                ApplicationName(name = "(+)"),
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
            EmptySemanticInfo(),
            Location.NoProvided
        ).inferType(module).apply {
            shouldBeRight(
                (0 until 3)
                    .map { longTypePromise.type.valueOrNull!! }
                    .toFunctionType()
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
                ApplicationName(name = "(+)"),
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
            EmptySemanticInfo(),
            Location.NoProvided
        ).inferType(module).apply {
            shouldBeRight(
                (0 until 3)
                    .map { longTypePromise.type.valueOrNull!! }.toFunctionType()
            )
        }
    }
    "Inference type over operators and variables with substitution" {
        val module = createInferenceInfo(ts)
        val longTypePromise = ts.getTypeSemanticInfo("Long")
        val param = module.module.functions["(+)"]!!.first().types.first().cast<Parameter>()
        val abstraction = AbstractionNode(
            "n",
            ApplicationNode(
                ApplicationName(name = "(+)"),
                listOf(
                    VarNode(
                        param.name,
                        TypeSemanticInfo(Either.Right(param)),
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
        abstraction.inferType(module).apply {
            shouldBeRight(
                (0 until 3)
                    .map { longTypePromise.type.valueOrNull!! }.toFunctionType()
            )
        }
        abstraction.expression.cast<ApplicationNode<SemanticInfo>>()
            .arguments.first().info.getInferredType(Location.NoProvided)
            .shouldBeRight(longTypePromise.type.valueOrNull!!)
    }
})
