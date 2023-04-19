package org.ksharp.semantics.inference

import InferenceInfo
import inferType
import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Location
import org.ksharp.module.prelude.preludeModule
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.nodes.semantic.ApplicationName
import org.ksharp.nodes.semantic.ApplicationNode
import org.ksharp.nodes.semantic.ConstantNode
import org.ksharp.semantics.nodes.EmptySemanticInfo
import org.ksharp.semantics.nodes.TypeSemanticInfo
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.types.newParameter

class InferenceTest : StringSpec({
    val module = InferenceInfo(preludeModule, preludeModule)
    val ts = preludeModule.typeSystem
    val longTypePromise = ts.getTypePromise("Long")
    "Inference type over constants" {
        AbstractionNode(
            "ten", ConstantNode(
                10.toLong(),
                TypeSemanticInfo(longTypePromise),
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
                        TypeSemanticInfo(longTypePromise),
                        Location.NoProvided
                    ),
                    ConstantNode(
                        2.toLong(),
                        TypeSemanticInfo(longTypePromise),
                        Location.NoProvided
                    )
                ),
                TypeSemanticInfo(ResolvedTypePromise(newParameter())),
                Location.NoProvided
            ),
            EmptySemanticInfo,
            Location.NoProvided
        ).inferType(module).apply {
            shouldBeRight(longTypePromise.type.valueOrNull!!)
        }
    }
})
