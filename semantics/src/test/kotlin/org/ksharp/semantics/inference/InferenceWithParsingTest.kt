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
    "Inference let expression - variable binding" {
        """
            fn = let a = 10 
                 then a
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldInferredTypesBe(
                "fn :: (Unit -> (Num numeric<Long>))"
            )
    }
    "Inference let expression - tuple variable binding" {
        """
            fn = let x, y = 10, 20 
                 then x + y
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldInferredTypesBe(
                "fn :: (Unit -> (Num numeric<Long>))"
            )
    }
    "Inference let expression - tuple variable binding 2" {
        """
            fn = let 10, y = 10, 20 
                 then y
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldInferredTypesBe(
                "fn :: (Unit -> (Num numeric<Long>))"
            )
    }
    "Inference let expression - not a tuple binding" {
        """
            fn = let x, y = 20 
                 then x + y
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeLeft(
                listOf(
                    InferenceErrorCode.NoATuple.new(
                        Location.NoProvided,
                        "type" to "(Num numeric<Long>)"
                    )
                )
            )
    }
    "Inference let expression - incompatible tuple binding" {
        """
            fn = let x, y = 20, 10, 30 
                 then x + y
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeLeft(
                listOf(
                    InferenceErrorCode.IncompatibleType.new(
                        Location.NoProvided,
                        "type" to "((Num numeric<Long>), (Num numeric<Long>), (Num numeric<Long>))"
                    )
                )
            )
    }
    "Inference let expression - list item binding" {
        """
            fn = let [x, y] = [20, 10, 30] 
                 then x + y
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldInferredTypesBe(
                "fn :: (Unit -> (Num numeric<Long>))"
            )
    }
    "Inference let expression - no a list" {
        """
            fn :: (Long, Long) -> Long
            fn a = let [x, y] = a 
                 then x + y
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeLeft(
                listOf(
                    InferenceErrorCode.NoAList.new(
                        Location.NoProvided,
                        "type" to "(Long, Long)"
                    )
                )
            )
    }
    "Inference let expression - incompatible list binding" {
        """
            fn :: (Map Int Int) -> Long
            fn a = let [x, y] = a 
                 then x + y
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeLeft(
                listOf(
                    InferenceErrorCode.NoAList.new(
                        Location.NoProvided,
                        "type" to "(Map Int Int)"
                    )
                )
            )
    }
    "Inference let expression - list binding with tail" {
        """
            fn = let [x, y | rest] = [20, 10, 30] 
                 then x + y
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldInferredTypesBe(
                "fn :: (Unit -> (Num numeric<Long>))"
            )
    }
    "Inference let expression - binding with guards" {
        """
            isEven :: Long -> Bool
            isEven a = True
            
            fn = let x && isEven x && isEven x = 10 
                 then x
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldInferredTypesBe(
                "isEven :: (Long -> True)",
                "fn :: (Unit -> (Num numeric<Long>))"
            )
    }
    "Inference let expression - list binding used as guard" {
        """
            fn = let x && [1, 2 | rest] = 10 
                 then a
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeLeft(
                listOf(
                    InferenceErrorCode.BindingUsedAsGuard.new(
                        Location.NoProvided
                    )
                )
            )
    }
    "Inference let expression - type binding" {
        """
            fn = let Bool a = True 
                 then a
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldInferredTypesBe(
                "fn :: (Unit -> True\n|False)"
            )
    }
})
