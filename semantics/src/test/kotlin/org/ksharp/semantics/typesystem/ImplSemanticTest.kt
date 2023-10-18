package org.ksharp.semantics.typesystem

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.Either
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.module.Impl
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.nodes.semantic.ApplicationName
import org.ksharp.nodes.semantic.ApplicationNode
import org.ksharp.nodes.semantic.VarNode
import org.ksharp.semantics.nodes.AbstractionSemanticInfo
import org.ksharp.semantics.nodes.ApplicationSemanticInfo
import org.ksharp.semantics.nodes.Symbol
import org.ksharp.semantics.nodes.TypeSemanticInfo
import org.ksharp.semantics.toSemanticModuleInfo
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.TypeSystemErrorCode
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.types.alias
import org.ksharp.typesystem.types.newNamedParameter

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
                    TypeSemanticsErrorCode.DuplicateImpl.new(Location.NoProvided, "trait" to "Sum", "impl" to "(Num a)")
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
            .shouldBeRight()
            .map {
                it.impls.shouldBe(
                    setOf(Impl("Sum", it.typeSystem["Num"].valueOrNull!!))
                )
            }
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
            .shouldBeRight()
            .map {
                it.impls.shouldBe(
                    setOf(Impl("Eq", it.typeSystem["Num"].valueOrNull!!))
                )
            }
    }
    "Error in method impl" {
        """
            trait Eq a =
                (=) :: a -> a -> Bool
                (!=) :: a -> a -> Bool
                (=) a b = a == b
            
            impl Eq for Num =
                (!=) a b = a != b
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeRight()
            .map {
                val paramA = TypeSemanticInfo(type = Either.Right(it.typeSystem.newNamedParameter("a")))
                it.implAbstractions
                    .shouldBe(
                        mapOf(
                            Impl("Eq", it.typeSystem["Num"].valueOrNull!!)
                                    to listOf(
                                AbstractionNode(
                                    attributes = setOf(CommonAttribute.Public),
                                    name = "(!=)",
                                    expression = ApplicationNode(
                                        functionName = ApplicationName(pck = null, name = "(!=)"),
                                        arguments = listOf(
                                            VarNode(
                                                name = "a",
                                                info = Symbol(
                                                    "a",
                                                    paramA
                                                ),
                                                location = Location.NoProvided
                                            ),
                                            VarNode(
                                                name = "b",
                                                info = Symbol(
                                                    "b",
                                                    paramA
                                                ),
                                                location = Location.NoProvided
                                            )
                                        ),
                                        info = ApplicationSemanticInfo(function = null),
                                        location = Location.NoProvided
                                    ),
                                    info = AbstractionSemanticInfo(
                                        parameters = listOf(
                                            Symbol(
                                                name = "a",
                                                type = paramA
                                            ), Symbol(name = "b", type = paramA)
                                        ),
                                        returnType = TypeSemanticInfo(type = it.typeSystem.alias("Bool"))
                                    ),
                                    location = Location.NoProvided
                                )
                            )
                        )
                    )
            }
    }
})
