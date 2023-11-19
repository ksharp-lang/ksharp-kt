package org.ksharp.semantics.typesystem

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import org.ksharp.common.*
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.nodes.semantic.ApplicationName
import org.ksharp.nodes.semantic.ApplicationNode
import org.ksharp.nodes.semantic.VarNode
import org.ksharp.semantics.getSemanticModuleInfo
import org.ksharp.semantics.nodes.AbstractionSemanticInfo
import org.ksharp.semantics.nodes.ApplicationSemanticInfo
import org.ksharp.semantics.nodes.Symbol
import org.ksharp.semantics.nodes.TypeSemanticInfo
import org.ksharp.semantics.scopes.TableErrorCode
import org.ksharp.semantics.toSemanticModuleInfo
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.TypeSystemErrorCode
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.types.TraitType
import org.ksharp.typesystem.types.newNamedParameter
import org.ksharp.typesystem.types.toFunctionType
import org.ksharp.typesystem.types.toParametricType

private fun Collection<TraitType>.shouldDefine(methods: Map<String, Boolean>): Collection<TraitType> {
    asSequence().map { t ->
        t.methods.asSequence().map { m ->
            "${t.name}::${m.key}" to m.value.withDefaultImpl
        }
    }.flatten()
        .toMap()
        .shouldBe(methods)
    return this
}

private fun TypeSystem.getTraits() =
    asSequence()
        .map { it.second }
        .filterIsInstance<TraitType>()
        .toList()

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
            .map {
                it.typeSystem
                    .getTraits()
                    .shouldDefine(mapOf("Sum::sum/3" to true))
                    .map { t -> t.representation }
            }
            .shouldBeRight(
                listOf(
                    """
                    trait Sum a =
                        sum :: a -> a -> a
                    """.trimIndent()
                )
            )
    }

    "Valid trait" {
        """
            trait Sum a =
                sum :: a -> a -> a
        """.trimIndent()
            .toSemanticModuleInfo()
            .map {
                it.typeSystem
                    .getTraits()
                    .shouldDefine(mapOf("Sum::sum/3" to false))
                    .map { t -> t.representation }
            }
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
              mul a a = a * b
        """.trimIndent()
            .getSemanticModuleInfo()
            .shouldBeRight()
            .map {
                val paramA = it.typeSystem.newNamedParameter("a")
                val addA = it.typeSystem["Add"].valueOrNull!!.cast<TraitType>().toParametricType()

                it.typeSystem
                    .getTraits()
                    .shouldNotBeEmpty()
                    .shouldDefine(mapOf("Sum::sum/3" to true))
                val paramAType = TypeSemanticInfo(type = Either.Right(paramA))
                it.traitsAbstractions["Sum"]
                    .also {
                        val abs = it!!.first()
                        println(abs)
                    }
                    .shouldBe(
                        listOf(
                            AbstractionNode(
                                attributes = setOf(CommonAttribute.Public),
                                name = "sum",
                                expression = ApplicationNode(
                                    functionName = ApplicationName(pck = null, name = "(+)"),
                                    arguments = listOf(
                                        VarNode(
                                            name = "a",
                                            info = Symbol(
                                                name = "a",
                                                type = paramAType
                                            ),
                                            location = Location.NoProvided
                                        ), VarNode(
                                            name = "b",
                                            info = Symbol(
                                                name = "b",
                                                type = paramAType
                                            ),
                                            location = Location.NoProvided
                                        )
                                    ), info = ApplicationSemanticInfo(
                                        function = listOf(addA, addA, addA).toFunctionType(
                                            MockHandlePromise(),
                                            NoAttributes
                                        )
                                    ), location = Location.NoProvided
                                ),
                                info = AbstractionSemanticInfo(
                                    parameters = listOf(
                                        Symbol(
                                            name = "a",
                                            type = paramAType
                                        ), Symbol(name = "b", type = paramAType)
                                    ),
                                    returnType = paramAType
                                ),
                                location = Location.NoProvided
                            )
                        )
                    )
                it.errors.shouldBe(
                    listOf(
                        TableErrorCode.AlreadyDefined.new(
                            Location.NoProvided,
                            "classifier" to "Variable",
                            "name" to "a"
                        ),
                    )
                )
            }
    }
})
