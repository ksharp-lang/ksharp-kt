package org.ksharp.semantics.typesystem

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.Either
import org.ksharp.common.Location
import org.ksharp.common.new
import org.ksharp.module.Impl
import org.ksharp.module.prelude.preludeModule
import org.ksharp.nodes.semantic.*
import org.ksharp.semantics.nodes.AbstractionSemanticInfo
import org.ksharp.semantics.nodes.ApplicationSemanticInfo
import org.ksharp.semantics.nodes.Symbol
import org.ksharp.semantics.nodes.TypeSemanticInfo
import org.ksharp.semantics.solve
import org.ksharp.semantics.toSemanticModuleInfo
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.TypeSystemErrorCode
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.types.newParameterForTesting
import org.ksharp.typesystem.types.toFunctionType

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
                (&) :: a -> a -> a
            
            impl Sum for Num =
                (&) a b = a + b
            
            impl Sum for Num =
                (&) a b = a + b
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeLeft(
                listOf(
                    TypeSemanticsErrorCode.DuplicateImpl.new(
                        Location.NoProvided,
                        "trait" to "Sum",
                        "impl" to preludeModule.typeSystem.solve("Num").representation
                    )
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
    "Trait implementing another trait" {
        """
            trait Eq2 a =
                eq :: a -> a -> Bool
                
            trait Eq a =
                (=) :: a -> a -> Bool
                (!=) :: a -> a -> Bool
            
            impl Eq for Eq2 =
                (=) a b = True
                (!=) a b = False
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeLeft(
                listOf(
                    TypeSemanticsErrorCode.TraitImplementingAnotherTrait.new(
                        Location.NoProvided,
                        "trait" to "Eq",
                        "impl" to "Eq2"
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
                (=) a b = True
            
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
    "Impl semantics nodes" {
        """
            trait Eq a =
                (=) :: a -> a -> Bool
                (!=) :: a -> a -> Bool
                (=) a b = True
            
            impl Eq for Num =
                support = True
                 
                (!=) a b = a != b
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeRight()
            .map {
                val boolType = it.typeSystem["Bool"].valueOrNull!!
                val forType = it.typeSystem["Num"]
                val unitType = it.typeSystem["Unit"]
                it.implAbstractions
                    .shouldBe(
                        mapOf(
                            Impl("Eq", forType.valueOrNull!!)
                                    to listOf(
                                AbstractionNode(
                                    attributes = setOf(CommonAttribute.Internal),
                                    name = "support",
                                    expression = ApplicationNode(
                                        functionName = ApplicationName(pck = null, name = "True"),
                                        arguments = listOf(
                                            ConstantNode(
                                                value = Unit,
                                                info = TypeSemanticInfo(type = unitType), Location.NoProvided
                                            )
                                        ),
                                        info = ApplicationSemanticInfo(function = null), Location.NoProvided
                                    ),
                                    info = AbstractionSemanticInfo(
                                        parameters = listOf(),
                                        returnType = TypeSemanticInfo(Either.Right(newParameterForTesting(2)))
                                    ), Location.NoProvided
                                ),
                                AbstractionNode(
                                    attributes = setOf(CommonAttribute.Public),
                                    name = "(!=)",
                                    expression = ApplicationNode(
                                        functionName = ApplicationName(pck = null, name = "(!=)"),
                                        arguments = listOf(
                                            VarNode(
                                                "a", Symbol(name = "a", TypeSemanticInfo(forType)),
                                                Location.NoProvided
                                            ),
                                            VarNode("b", Symbol("b", TypeSemanticInfo(forType)), Location.NoProvided)
                                        ),
                                        info = ApplicationSemanticInfo(
                                            function = listOf(
                                                forType.valueOrNull!!,
                                                forType.valueOrNull!!,
                                                boolType
                                            ).toFunctionType(it.typeSystem)
                                        ), Location.NoProvided
                                    ),
                                    info = AbstractionSemanticInfo(
                                        parameters = listOf(
                                            Symbol("a", TypeSemanticInfo(forType)),
                                            Symbol("b", TypeSemanticInfo(forType))
                                        ),
                                        returnType = TypeSemanticInfo(Either.Right(boolType))
                                    ),
                                    Location.NoProvided
                                )
                            )
                        )
                    )
            }
    }
})
