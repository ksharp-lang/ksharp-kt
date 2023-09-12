package org.ksharp.semantics.typesystem

import io.kotest.core.spec.style.StringSpec
import org.ksharp.semantics.toSemanticModuleInfo
import org.ksharp.test.shouldBeLeft

class TraitSemanticTest : StringSpec({
    "Not allow duplicate functions. (Same name and arity)" {
        """
            trait Sum a =
             sum :: a -> a -> a
             sum :: a -> a -> a
        """.trimIndent()
            .toSemanticModuleInfo()
            .mapLeft {
                println(it)
            }.shouldBeLeft(emptyList<Error>())
    }
})
