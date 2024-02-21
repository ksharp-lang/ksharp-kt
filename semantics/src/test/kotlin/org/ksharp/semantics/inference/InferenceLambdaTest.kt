package org.ksharp.semantics.inference

import io.kotest.core.spec.style.StringSpec
import org.ksharp.semantics.toSemanticModuleInfo
import org.ksharp.test.shouldBeRight

class InferenceLambdaTest : StringSpec({
    "Inference unit lambda" {
        """
            fun doubleFn = \a -> a * 2
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeRight()
            .map {
                TODO()
            }
    }
    "Inference lambda with arguments" {}
    "Inference closure unit lambda" {}
    "Inference closure lambda with arguments" {}
    "Inference pass lambda as high order function" {}
})
