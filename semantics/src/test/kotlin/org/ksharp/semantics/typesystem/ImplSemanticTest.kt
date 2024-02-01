package org.ksharp.semantics.typesystem

import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.shouldBe
import org.ksharp.common.Either
import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.common.new
import org.ksharp.module.Impl
import org.ksharp.module.prelude.preludeModule
import org.ksharp.nodes.semantic.*
import org.ksharp.semantics.inference.InferenceErrorCode
import org.ksharp.semantics.solve
import org.ksharp.semantics.toSemanticModuleInfo
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.TypeSystemErrorCode
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.types.ImplType
import org.ksharp.typesystem.types.newParameterForTesting
import org.ksharp.typesystem.types.resetParameterCounterForTesting
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
            
            impl Sum for Int =
                (&) a b = a + b
            
            impl Sum for Int =
                (&) a b = a + b
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeLeft(
                listOf(
                    TypeSemanticsErrorCode.DuplicateImpl.new(
                        Location.NoProvided,
                        "trait" to "Sum",
                        "impl" to preludeModule.typeSystem.solve("Int").representation
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
                    TypeSemanticsErrorCode.DuplicateImplMethod.new(Location.NoProvided, "name" to "(+)/2")
                )
            )
    }
    "Missing method implementation in Impls" {
        """
            trait Eq a =
                (=) :: a -> a -> Bool
                (!=) :: a -> a -> Bool
            
            impl Eq for Int =
                (=) a b = a === b
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeLeft(
                listOf(
                    InferenceErrorCode.FunctionNotFound.new(
                        Location.NoProvided,
                        "function" to "(===) Int Int"
                    ),
                    TypeSemanticsErrorCode.MissingImplMethods.new(
                        "methods" to "(=)/2, (!=)/2",
                        "impl" to "Int",
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
            
            impl Sum for Int =
                (+) a b = a + b
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeRight()
            .map {
                it.impls.shouldBe(
                    setOf(Impl("irTest", "Sum", it.typeSystem["Int"].valueOrNull!!))
                )
            }
    }
    "Impl with missing method, but it has default implementation in trait" {
        """
            trait Eq a =
                (=) :: a -> a -> Bool
                (!=) :: a -> a -> Bool
                (=) a b = True
            
            impl Eq for Int =
                (!=) a b = a != b
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeRight()
            .map {
                it.impls.shouldBe(
                    setOf(Impl("irTest", "Eq", it.typeSystem["Int"].valueOrNull!!))
                )
            }
    }
    "Impl semantics nodes" {
        """
            trait Eq a =
                (=) :: a -> a -> Bool
                (!=) :: a -> a -> Bool
                (=) a b = True
            
            impl Eq for Int =
                support = True
                 
                (!=) a b = a != b
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeRight()
            .map {
                val boolType = it.typeSystem["Bool"].valueOrNull!!
                val forType = it.typeSystem["Int"]
                val unitType = it.typeSystem["Unit"]
                val implType = ImplType(it.typeSystem["Eq"].valueOrNull!!.cast(), forType.valueOrNull!!)
                val expectedAbstractions = listOf(
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
                            listOf(),
                            returnType = TypeSemanticInfo(Either.Right(newParameterForTesting(1)))
                        ), Location.NoProvided
                    ),
                    AbstractionNode(
                        attributes = setOf(CommonAttribute.TraitMethod, CommonAttribute.Public),
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
                                    implType,
                                    implType,
                                    boolType
                                ).toFunctionType(it.typeSystem, setOf(CommonAttribute.TraitMethod))
                            ), Location.NoProvided
                        ),
                        info = AbstractionSemanticInfo(
                            listOf(
                                Symbol("a", TypeSemanticInfo(forType)),
                                Symbol("b", TypeSemanticInfo(forType))
                            ),
                            returnType = TypeSemanticInfo(Either.Right(boolType))
                        ),
                        Location.NoProvided
                    )
                )
                it.implAbstractions
                    .shouldBe(
                        mapOf(
                            Impl("irTest", "Eq", forType.valueOrNull!!)
                                    to expectedAbstractions
                        )
                    )
            }
    }
}) {
    override suspend fun beforeAny(testCase: TestCase) {
        resetParameterCounterForTesting()
    }
}
