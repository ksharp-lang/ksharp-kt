package org.ksharp.semantics.typesystem

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.module.Impl
import org.ksharp.semantics.toSemanticModuleInfo
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.TypeSystemErrorCode

class ImplSemanticTest : StringSpec({
    "Trait not defined" {
        """
            impl Sum for Num =
                (+) a b = a + b
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeLeft(
                listOf(
                    TypeSystemErrorCode.TypeNotFound.new("type" to "Sum")
                )
            )
    }
    "Not allow duplicate Impls" {
        """
            trait Sum a =
                (+) :: a -> a -> a
            
            impl Sum for Num =
                (+) a b = a + b
            
            impl Sum for Num =
                (+) a b = a + b
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeLeft(
                listOf(
                    TypeSemanticsErrorCode.DuplicateImpl.new(Location.NoProvided, "trait" to "Sum", "impl" to "Num")
                )
            )
    }
    "Not allow duplicate methods in Impls" {
        """
            trait Sum a =
                (+) :: a -> a -> a
            
            impl Sum for Num =
                (+) a b = a + b
                (+) a b = a + b
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeLeft(
                listOf(
                    TypeSemanticsErrorCode.DuplicateImplMethod.new(Location.NoProvided, "name" to "(+)/3")
                )
            )
    }
    "Missing method implementation in Impls" {
        """
            trait Eq a =
                (=) :: a -> a -> Bool
                (!=) :: a -> a -> Bool
            
            impl Eq for Num =
                (=) a b = a == b
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeLeft(
                listOf(
                    TypeSemanticsErrorCode.MissingImplMethods.new(
                        Location.NoProvided,
                        "methods" to "(!=)/3",
                        "impl" to "Num",
                        "trait" to "Eq"
                    )
                )
            )
    }
    "Valid Impl" {
        """
            trait Sum a =
                (+) :: a -> a -> a
            
            impl Sum for Num =
                (+) a b = a + b
        """.trimIndent()
            .toSemanticModuleInfo()
            .map {
                it.impls
            }
            .shouldBeRight(
                setOf(Impl("Sum", "Num"))
            )
    }
    "Impl with missing method, but it has default implementation in trait" {
        """
            trait Eq a =
                (=) :: a -> a -> Bool
                (!=) :: a -> a -> Bool
                (=) a b = a == b
            
            impl Eq for Num =
                (!=) a b = a != b
        """.trimIndent()
            .toSemanticModuleInfo()
            .map {
                it.impls
            }
            .shouldBeRight(
                setOf(Impl("Eq", "Num"))
            )
    }
    "Error in method impl" {}
})
