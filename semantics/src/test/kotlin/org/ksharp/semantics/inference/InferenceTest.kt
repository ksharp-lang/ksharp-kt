package org.ksharp.semantics.inference

import inferType
import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Either
import org.ksharp.common.Location
import org.ksharp.module.ModuleInfo
import org.ksharp.module.moduleFunctions
import org.ksharp.module.prelude.preludeModule
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.nodes.semantic.ApplicationName
import org.ksharp.nodes.semantic.ApplicationNode
import org.ksharp.nodes.semantic.ConstantNode
import org.ksharp.semantics.nodes.EmptySemanticInfo
import org.ksharp.semantics.nodes.TypeSemanticInfo
import org.ksharp.semantics.nodes.getTypeSemanticInfo
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.TypeSystem
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
    val module = createInferenceInfo(ts)
    val longTypePromise = ts.getTypeSemanticInfo("Long")
    val intTypePromise = ts.getTypeSemanticInfo("Int")

    "Inference type over constants" {
        AbstractionNode(
            "ten", ConstantNode(
                10.toLong(),
                longTypePromise,
                Location.NoProvided
            ),
            EmptySemanticInfo,
            Location.NoProvided
        ).inferType(module).apply {
            shouldBeRight(longTypePromise.type.valueOrNull!!)
        }
    }
    "Inference type over operators" {
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
            EmptySemanticInfo,
            Location.NoProvided
        ).inferType(module).apply {
            shouldBeRight(
                (0 until 3)
                    .map { longTypePromise.type.valueOrNull!! }.toFunctionType()
            )
        }
    }
    "Inference type over operators with substitution" {
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
            EmptySemanticInfo,
            Location.NoProvided
        ).inferType(module).apply {
            shouldBeRight(
                (0 until 3)
                    .map { longTypePromise.type.valueOrNull!! }.toFunctionType()
            )
        }
    }
})
