package org.ksharp.semantics.typesystem

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.semantics.toSemanticModuleInfo
import org.ksharp.test.shouldBeLeft
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
                (+) a b = a + b
            
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
                (+) a b = a + b
            
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
    "Missing method implementation in Impls" {}
    "Valid Impl" {}
    "Impl with missing method, but it has default implementation in trait" {}
    "Error in method impl" {}
})
