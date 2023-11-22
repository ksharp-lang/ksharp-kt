package org.ksharp.semantics.inference

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.module.prelude.preludeModule
import org.ksharp.nodes.semantic.TypeSemanticInfo
import org.ksharp.semantics.nodes.getTypeSemanticInfo

class TypePromiseTest : StringSpec({
    "Check TypePromise instantiation" {
        preludeModule.typeSystem.apply {
            getTypeSemanticInfo("Int").shouldBe(TypeSemanticInfo(get("Int")))
            getTypeSemanticInfo("NotAType").shouldBe(TypeSemanticInfo(get("NotAType")))
        }
    }
})
