package org.ksharp.semantics.inference

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.ksharp.module.prelude.preludeModule
import org.ksharp.semantics.nodes.SemanticModuleInfo
import org.ksharp.semantics.toSemanticModuleInfo
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.types.TraitType
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.types.newParameter

private fun SemanticModuleInfo.traitsImplemented(type: Type): List<TraitType> =
    getTraitsImplemented(
        type,
        abstractions.toSemanticModuleInfo(
            typeSystem,
            impls,
            traits
        )
    ).toList()

class InferenceTraitsImplementedTest : StringSpec({
    "Find traits implemented by a parameter" {
        """
            trait Sum a =
             sum :: a -> a -> a
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeRight()
            .map {
                it.traitsImplemented(preludeModule.typeSystem.newParameter())
                    .shouldBe(
                        listOf(
                            it.typeSystem["Sum"].valueOrNull!!
                        )
                    )
            }
    }
    "Find traits implemented by a concrete type, module without implementations" {
        """
            trait Sum a =
             sum :: a -> a -> a
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeRight()
            .map {
                it.traitsImplemented(preludeModule.typeSystem["Int"].valueOrNull!!)
                    .shouldBeEmpty()
            }
    }
    "Find traits implemented by a concrete type, module with implementations" {
        """
            trait Sum a =
              sum :: a -> a -> a
            
            impl Sum for Int =
              sum x y = x + y
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeRight()
            .map {
                it.traitsImplemented(preludeModule.typeSystem.newParameter())
                    .shouldBe(
                        listOf(
                            it.typeSystem["Sum"].valueOrNull!!
                        )
                    )
            }
    }
})
