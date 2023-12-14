package org.ksharp.semantics.inference

import io.kotest.core.spec.style.StringSpec
import org.ksharp.semantics.nodes.toCodeModule
import org.ksharp.semantics.toSemanticModuleInfo
import org.ksharp.test.shouldBeRight

class ModuleDependenciesInferenceTest : StringSpec({
    "Inference application for external module" {
        val module1 = """
            sum2 = (+) 2
        """.trimIndent()
            .toSemanticModuleInfo()
            .map { it.toCodeModule() }
            .map { it.module }
            .shouldBeRight()
            .value

        """
            import org.module1 as m
            
            sum = m.sum2 3
        """.trimIndent()
            .toSemanticModuleInfo() { name, _ ->
                if (name == "org.module1") module1
                else null
            }
            .shouldBeRight()
            .shouldInferredTypesBe(
                "sum :: (Unit -> (Num numeric<Long>))"
            )
    }
    "Inference type instance application for external module" {
        val module1 = """
            type Bool2 = True2 | False2
        """.trimIndent()
            .toSemanticModuleInfo()
            .map { it.toCodeModule() }
            .map { it.module }
            .shouldBeRight()
            .value

        val module2 = """
            type Bool2 = True2 | False2
        """.trimIndent()
            .toSemanticModuleInfo()
            .map { it.toCodeModule() }
            .map { it.module }
            .shouldBeRight()
            .value

        """
            import org.module1 as m
            import org.module2 as m2
            
            true = m.True2
            false = m2.False2
        """.trimIndent()
            .toSemanticModuleInfo() { name, _ ->
                if (name == "org.module1") module1
                else if (name == "org.module2") module2
                else null
            }
            .shouldBeRight()
            .shouldInferredTypesBe(
                "true :: (Unit -> True2)",
                "false :: (Unit -> False2)"
            )
    }
})
