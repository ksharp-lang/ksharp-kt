package org.ksharp.compiler

import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.ksharp.common.*
import org.ksharp.module.prelude.preludeModule
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.nodes.semantic.AbstractionSemanticInfo
import org.ksharp.nodes.semantic.ConstantNode
import org.ksharp.nodes.semantic.TypeSemanticInfo
import org.ksharp.semantics.inference.InferenceErrorCode
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.types.newParameterForTesting
import org.ksharp.typesystem.types.resetParameterCounterForTesting

class CompilerTestModuleInfo : StringSpec({
    "Create a moduleinfo from a String" {
        """
        |@native 
        |ten = 10
        """.trimMargin()
            .moduleInfo("file1.ff", preludeModule)
            .shouldBeRight()
            .apply {
                map {
                    it.name.shouldBe("file1")
                    it.errors.shouldBeEmpty()
                    it.artifact.abstractions.shouldBe(
                        listOf(
                            AbstractionNode(
                                setOf(CommonAttribute.Internal),
                                name = "ten",
                                expression = ConstantNode(
                                    value = 10.toLong(),
                                    info = TypeSemanticInfo(type = it.module.typeSystem["Long"]),
                                    location = Location(
                                        Line(value = 2) to Offset(value = 6), Line(value = 2) to Offset(value = 8)
                                    )
                                ),
                                info = AbstractionSemanticInfo(
                                    emptyList(),
                                    TypeSemanticInfo(Either.Right(newParameterForTesting(0)))
                                ),
                                location = Location(
                                    Line(value = 2) to Offset(value = 0), Line(value = 2) to Offset(value = 3)
                                )
                            )
                        )
                    )
                }
            }
    }
    "Create a moduleinfo from a String 2" {
        """
        |ten = 10
        """.trimMargin()
            .moduleInfo("file1", preludeModule)
            .shouldBeRight()
            .apply {
                map {
                    it.name.shouldBe("file1")
                    it.errors.shouldBeEmpty()
                    it.artifact.abstractions.shouldBe(
                        listOf(
                            AbstractionNode(
                                setOf(CommonAttribute.Internal),
                                name = "ten",
                                expression = ConstantNode(
                                    value = 10.toLong(),
                                    info = TypeSemanticInfo(type = it.module.typeSystem["Long"]),
                                    location = Location(
                                        Line(value = 1) to Offset(value = 6), Line(value = 1) to Offset(value = 8)
                                    )
                                ),
                                info = AbstractionSemanticInfo(
                                    emptyList(),
                                    TypeSemanticInfo(Either.Right(newParameterForTesting(0)))
                                ),
                                location = Location(
                                    Line(value = 1) to Offset(value = 0), Line(value = 1) to Offset(value = 3)
                                )
                            )
                        )
                    )
                }
            }
    }
    "Create a moduleinfo from a String with error" {
        """
        |ten = no-exist-fun 10
        """.trimMargin()
            .moduleInfo("file1.ff", preludeModule)
            .shouldBeLeft(
                listOf(
                    InferenceErrorCode.FunctionNotFound.new(
                        Location(Line(1) to Offset(6), Line(value = 1) to Offset(value = 18)),
                        "function" to "no-exist-fun Long"
                    )
                )
            )
    }
}) {
    override suspend fun beforeAny(testCase: TestCase) {
        resetParameterCounterForTesting()
    }
}
