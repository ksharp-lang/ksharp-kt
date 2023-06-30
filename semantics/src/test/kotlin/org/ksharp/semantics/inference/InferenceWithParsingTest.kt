package org.ksharp.semantics.inference

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.ksharp.common.*
import org.ksharp.module.prelude.preludeModule
import org.ksharp.parser.ksharp.parseModule
import org.ksharp.semantics.nodes.SemanticModuleInfo
import org.ksharp.semantics.nodes.toSemanticModuleInfo
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight

fun String.toSemanticModuleInfo(): Either<List<Error>, SemanticModuleInfo> =
    this.parseModule("irTest.ks", false)
        .flatMap {
            val moduleInfo = it.toSemanticModuleInfo(preludeModule)
            if (moduleInfo.errors.isNotEmpty()) {
                Either.Left(moduleInfo.errors)
            } else Either.Right(moduleInfo)
        }

fun Either<List<Error>, SemanticModuleInfo>.shouldInferredTypesBe(vararg types: String) {
    shouldBeRight().value.apply {
        abstractions.size.shouldBe(types.size)
        abstractions.map {
            it.info.getInferredType(Location.NoProvided)
                .map { type ->
                    "${it.name} :: ${type.representation}"
                }
        }.unwrap().shouldBeRight()
            .value.shouldContainExactlyInAnyOrder(types.toList())
    }
}

class InferenceWithParsingTest : StringSpec({
    "Inference module" {
        """
            fn = sum 10 20
            sum a b = a + b
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldInferredTypesBe(
                "sum :: ((Num a) -> (Num a) -> (Num a))",
                "fn :: (Unit -> (Num numeric<Long>))"
            )
    }
    "Inference module 2" {
        """
            sum a b = a + b
            fn = sum 10 20
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldInferredTypesBe(
                "sum :: ((Num a) -> (Num a) -> (Num a))",
                "fn :: (Unit -> (Num numeric<Long>))"
            )
    }
    "Inference module - function not found" {
        """
            sum a b = a + b
            fn = sum 10
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeLeft(
                listOf(
                    InferenceErrorCode.FunctionNotFound.new(
                        Location.NoProvided,
                        "function" to "sum (Num numeric<Long>)"
                    )
                )
            )
    }
    "Inference module - function not found 2" {
        """
            fn = (+) 10
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeLeft(
                listOf(
                    InferenceErrorCode.FunctionNotFound.new(
                        Location.NoProvided,
                        "function" to "(+) (Num numeric<Long>)"
                    )
                )
            )
    }
})
