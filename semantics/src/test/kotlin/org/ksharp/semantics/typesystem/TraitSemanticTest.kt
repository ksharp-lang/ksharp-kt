package org.ksharp.semantics.typesystem

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Location
import org.ksharp.common.new
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
            .shouldBeLeft(
                listOf(
                    TypeSemanticsErrorCode.DuplicateTraitMethod.new(Location.NoProvided, "name" to "sum/3")
                )
            )
    }
    "Trait method not a function type" {
        """
            trait Sum a =
             sum :: Map String Int
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeLeft(
                listOf(
                    TypeSemanticsErrorCode.TraitMethodShouldBeAFunctionType.new(Location.NoProvided, "name" to "sum")
                )
            )
    }

    "Trait with two functions with same arity and name" {
        """
            trait Sum a =
             sum :: a -> a -> a
             
             sum a b = a + b
             sum a b = a + b
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeLeft(
                listOf(
                    TypeSemanticsErrorCode.DuplicateTraitMethod.new(Location.NoProvided, "name" to "sum/3")
                )
            )
    }
})
