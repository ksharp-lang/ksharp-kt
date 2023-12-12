package org.ksharp.semantics.inference

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.ksharp.common.*
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.nodes.semantic.SemanticInfo
import org.ksharp.semantics.nodes.SemanticModuleInfo
import org.ksharp.semantics.nodes.toCodeModule
import org.ksharp.semantics.toSemanticModuleInfo
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.TypeSystemErrorCode

private fun List<AbstractionNode<SemanticInfo>>.stringRepresentation(prefix: String) =
    map {
        it.info.getInferredType(Location.NoProvided)
            .map { type ->
                "${prefix}${it.name} :: ${type.representation}"
            }
    }.unwrap()

private fun Either<List<Error>, SemanticModuleInfo>.shouldInferredTypesBe(vararg types: String) {
    shouldBeRight().value.apply {
        abstractions.stringRepresentation("")
            .shouldBeRight()
            .value
            .onEach(::println)
            .shouldContainExactlyInAnyOrder(types.toList())
    }
}

private fun Either<List<Error>, SemanticModuleInfo>.shouldInferredTraitAbstractionsTypesBe(
    vararg types: String
) {
    shouldBeRight().value.apply {
        traitsAbstractions.map {
            it.value.stringRepresentation("${it.key} :: ")
        }.unwrap().shouldBeRight()
            .value
            .flatten()
            .onEach(::println)
            .shouldContainExactlyInAnyOrder(types.toList())
    }
}

private fun Either<List<Error>, SemanticModuleInfo>.shouldInferredImplAbstractionsTypesBe(
    vararg types: String
) {
    shouldBeRight().value.apply {
        implAbstractions.map {
            it.value.stringRepresentation("${it.key.trait} for ${it.key.type} :: ")
        }.unwrap().shouldBeRight()
            .value
            .flatten()
            .shouldContainExactlyInAnyOrder(types.toList())
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
                "sum :: ((Add a) -> (Add a) -> (Add a))",
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
                "sum :: ((Add a) -> (Add a) -> (Add a))",
                "fn :: (Unit -> (Num numeric<Long>))"
            )
    }
    "Inference module - function not found" {
        """
            sum a b = a + b
            fn = sum2 10
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeLeft(
                listOf(
                    InferenceErrorCode.FunctionNotFound.new(
                        Location.NoProvided,
                        "function" to "sum2 (Num numeric<Long>)"
                    )
                )
            )
    }
    "Inference module - function not found 2" {
        """
            fn = (+) "Hello"
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeLeft(
                listOf(
                    InferenceErrorCode.FunctionNotFound.new(
                        Location.NoProvided,
                        "function" to "(+) String"
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
                "isEven :: ((Num numeric<Long>) -> True)",
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
    "Inference match expression" {
        """
            ten = 10
            fn = match ten with
                       10 then ten
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldInferredTypesBe(
                "ten :: (Unit -> (Num numeric<Long>))",
                "fn :: (Unit -> (Num numeric<Long>))"
            )
    }
    "Inference match expression 2" {
        """
            fn = match [1, 2] with
                       [x, y] then x + y
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldInferredTypesBe(
                "fn :: (Unit -> (Num numeric<Long>))"
            )
    }
    "Inference match expression with error" {
        """
            fn = match [1, 2] with
                       [x, y] then x + y
                       z then True
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeLeft(
                listOf(
                    TypeSystemErrorCode.IncompatibleTypes.new(
                        Location.NoProvided,
                        "type1" to "(Num numeric<Long>)",
                        "type2" to "True"
                    )
                )
            )
    }
    "Inference trait abstraction" {
        """
            ten = int 10
            
            trait Op a =
              len :: a -> Int
              len a = ten
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldInferredTraitAbstractionsTypesBe(
                "Op :: len :: (a -> (Num numeric<Int>))"
            )
    }
    "Inference impl abstraction" {
        """
           trait Op a =
             sum :: a -> a -> a
           
           impl Op for Int =
             sum a b = a + b
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldInferredImplAbstractionsTypesBe(
                "Op for Num numeric<Int> :: sum :: ((Num numeric<Int>) -> (Num numeric<Int>) -> (Num numeric<Int>))"
            )
    }
    "Inference trait used in a function" {
        """
           trait Op a =
             sum :: a -> a -> a
           
           impl Op for Int =
             sum a b = a + b
           
           fn a :: (Op a) -> (Op a) -> (Op a)
           fn a b = sum a b
           
           s = fn (int 10) (int 20)
           
           s2 = s
        """.trimIndent()
            .toSemanticModuleInfo()
            .apply {
                shouldBeRight()
                shouldInferredImplAbstractionsTypesBe(
                    "Op for Num numeric<Int> :: sum :: ((Num numeric<Int>) -> (Num numeric<Int>) -> (Num numeric<Int>))"
                )
                shouldInferredTypesBe(
                    "fn :: ((Op a) -> (Op a) -> (Op a))",
                    "s :: (Unit -> (Op a))",
                    "s2 :: (Unit -> (Op a))"
                )
            }
    }

    "Inference impl using a default trait method" {
        """
            trait Op a =
              sum :: a -> a -> a
              sum10 :: a -> a
              
              sum a b = a + b
            
            impl Op for Int =
              sum10 a = sum (int 10) a
        """.trimIndent()
            .toSemanticModuleInfo()
            .apply {
                shouldInferredImplAbstractionsTypesBe(
                    "Op for Num numeric<Int> :: sum10 :: ((Num numeric<Int>) -> (Num numeric<Int>))"
                )
                shouldInferredTraitAbstractionsTypesBe(
                    "Op :: sum :: ((Add a) -> (Add a) -> (Add a))"
                )
            }
    }

    "Inference parametric function" {
        """
            emptyHashMap k v :: () -> (Map k v)
            native pub emptyHashMap
        """.trimIndent()
            .toSemanticModuleInfo()
            .apply {
                shouldBeRight()
                valueOrNull!!
                    .toCodeModule()
                    .module
                    .functions
                    .keys
                    .shouldBe(
                        listOf(
                            "emptyHashMap/0"
                        )
                    )
            }
            .shouldInferredTypesBe(
                "emptyHashMap :: (Unit -> (Map k v))"
            )
    }
    "Inference partial application" {
        """
            sum a b = a + b
            sum2 = sum 2
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldInferredTypesBe(
                "sum :: ((Add a) -> (Add a) -> (Add a))",
                "sum2 :: ((Num numeric<Long>) -> (Num numeric<Long>))"
            )
    }
    "Inference partial application over prelude function" {
        """
            keyValue = pair "Hello"
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldInferredTypesBe(
                "keyValue :: (b -> (Pair String b))"
            )
    }
    "Inference partial application used in a function" {
        """
            sum a b = a + b
            sum2 = sum 2           
            fn a = sum2 a
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldInferredTypesBe(
                "sum :: ((Add a) -> (Add a) -> (Add a))",
                "sum2 :: ((Num numeric<Long>) -> (Num numeric<Long>))",
                "fn :: ((Num numeric<Long>) -> (Num numeric<Long>))"
            )
    }
    "Inference partial application used in a function, partial declared after used" {
        """
            sum a b = a + b
            fn a = sum2 a
            sum2 = sum 2
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldInferredTypesBe(
                "sum :: ((Add a) -> (Add a) -> (Add a))",
                "sum2 :: ((Num numeric<Long>) -> (Num numeric<Long>))",
                "fn :: ((Num numeric<Long>) -> (Num numeric<Long>))"
            )
    }
    "Inference partial application in abstraction with parameters" {
        """
            sum a b = a + b
            sumN a = sum a
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldInferredTypesBe(
                "sum :: ((Add a) -> (Add a) -> (Add a))",
                "sumN :: ((Add a) -> ((Add a) -> (Add a)))"
            )
    }
    "Inference partial module trait application in an abstraction" {
        """
            sum10 = (+) 10
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldInferredTypesBe(
                "sum10 :: ((Num numeric<Long>) -> (Num numeric<Long>))"
            )
    }
    "Inference partial trait application in an abstraction" {
        """
            trait Op a =
              sum :: a -> Long -> a
            
            sumN op = sum op
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldInferredTypesBe(
                "sumN :: ((Op a) -> ((Num numeric<Long>) -> (Op a)))"
            )
    }
    "Inference partial application into a trait" {
        """
            trait Op a =
              sum :: a -> Long -> a
              sum2 :: a -> a
              
              sumN n a = sum a n
              sum2 = sumN 10
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldInferredTraitAbstractionsTypesBe(
                "Op :: sumN :: ((Num numeric<Long>) -> (Op a) -> (Op a))",
                "Op :: sum2 :: ((Op a) -> (Op a))"
            )
    }
})
