package org.ksharp.semantics.typesystem

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.semantics.toSemanticModuleInfo
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.TypeSystemErrorCode

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

    "Duplicate traits" {
        """
            trait Sum a =
              sum :: a -> a -> a
              sum a b = a + b
            
            trait Sum a =
              sum2 :: a -> a -> a
              sum2 a b = a + b
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeLeft(
                listOf(
                    TypeSystemErrorCode.TypeAlreadyRegistered.new("type" to "Sum")
                )
            )
    }

    "Valid trait with default implementation" {
        """
            trait Sum a =
             sum :: a -> a -> a

             sum a b = a + b
        """.trimIndent()
            .toSemanticModuleInfo()
            .map { it.traits.map { t -> t.representation } }
            .shouldBeRight(
                listOf(
                    """
                    trait Sum a =
                        sum :: a -> a -> a
                    """.trimIndent()
                )
            )
    }

    "Invalid trait function" {
        """
            trait Sum a =
              sum :: a -> a -> a
              sum a b = a + b
              mul a b = a * b
        """.trimIndent()
            .also { println(it) }
            .toSemanticModuleInfo()
            .shouldBeRight(
                listOf(
                    """
                    trait Sum a =
                        sum :: a -> a -> a
                    """.trimIndent()
                )
            )
    }
})
