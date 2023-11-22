package org.ksharp.semantics.nodes

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.cast
import org.ksharp.module.prelude.preludeModule
import org.ksharp.nodes.FunctionNode
import org.ksharp.nodes.FunctionTypeNode
import org.ksharp.nodes.TraitFunctionNode
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.nodes.semantic.SemanticInfo
import org.ksharp.parser.ksharp.parseModule
import org.ksharp.semantics.expressions.nameWithArity
import org.ksharp.semantics.inference.nameWithArity
import org.ksharp.semantics.typesystem.arity
import org.ksharp.semantics.typesystem.nameWithArity
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.types.TraitType

class ArityTest : StringSpec({
    "Check function arity" {
        "ten = 10"
            .parseModule("module.ks", false)
            .shouldBeRight()
            .map {
                it.functions
                    .map(FunctionNode::nameWithArity)
                    .shouldBe(listOf("ten/0"))
            }
    }
    "Check function arity with arguments" {
        "ten a = a"
            .parseModule("module.ks", false)
            .shouldBeRight()
            .map {
                it.functions
                    .map(FunctionNode::nameWithArity)
                    .shouldBe(listOf("ten/1"))
            }
    }
    "Check type function arity" {
        "ten :: () -> Int"
            .parseModule("module.ks", false)
            .shouldBeRight()
            .map {
                it.typeDeclarations.first()
                    .type.cast<FunctionTypeNode>()
                    .arity
                    .shouldBe(0)
            }
    }
    "Check type function arity with one arguments" {
        "ten :: Int -> Int"
            .parseModule("module.ks", false)
            .shouldBeRight()
            .map {
                it.typeDeclarations.first()
                    .type.cast<FunctionTypeNode>()
                    .arity
                    .shouldBe(1)
            }
    }
    "Check type function arity with many arguments" {
        "ten :: Int -> Int -> Int"
            .parseModule("module.ks", false)
            .shouldBeRight()
            .map {
                it.typeDeclarations.first()
                    .type.cast<FunctionTypeNode>()
                    .arity
                    .shouldBe(2)
            }
    }
    "Check trait function arity" {
        """trait Sum =
          |  ten :: () -> Int
          |  ten :: Int -> Int
          |  sum :: Int -> Int -> Int
          |
        """.trimMargin()
            .parseModule("module.ks", false)
            .shouldBeRight()
            .map {
                it.traits.first()
                    .definition.definitions
                    .map(TraitFunctionNode::nameWithArity)
                    .shouldBe(listOf("ten/0", "ten/1", "sum/2"))
            }
    }
    "Check trait type arity" {
        """trait Sum a =
          |  ten :: () -> a
          |  ten :: a -> a
          |  sum :: a -> a -> a
          |
        """.trimMargin()
            .parseModule("module.ks", false)
            .shouldBeRight()
            .map {
                it.toSemanticModuleInterface(preludeModule)
                    .toSemanticModuleInfo()
                    .typeSystem["Sum"].valueOrNull!!.cast<TraitType>()
                    .methods.values
                    .map(TraitType.MethodType::nameWithArity)
                    .shouldBe(listOf("ten/0", "ten/1", "sum/2"))
            }
    }
    "Check abstraction arity" {
        """ten = 10
          |
          |ten a = a
          |
          |sum a b = a + b
        """.trimMargin()
            .parseModule("module.ks", false)
            .shouldBeRight()
            .map {
                it.toSemanticModuleInterface(preludeModule)
                    .toSemanticModuleInfo()
                    .abstractions
                    .map(AbstractionNode<SemanticInfo>::nameWithArity)
                    .shouldBe(listOf("ten/0", "ten/1", "sum/2"))
            }
    }
})
