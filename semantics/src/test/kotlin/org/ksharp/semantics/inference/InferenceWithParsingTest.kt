package org.ksharp.semantics.inference

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.ksharp.common.Either
import org.ksharp.common.Location
import org.ksharp.common.unwrap
import org.ksharp.module.prelude.preludeModule
import org.ksharp.parser.ksharp.parseModule
import org.ksharp.semantics.nodes.SemanticModuleInfo
import org.ksharp.semantics.nodes.toSemanticModuleInfo
import org.ksharp.test.shouldBeRight

fun String.toSemanticModuleInfo() =
    this.parseModule("irTest.ks", false)
        .flatMap {
            val moduleInfo = it.toSemanticModuleInfo(preludeModule)
            if (moduleInfo.errors.isNotEmpty()) {
                Either.Left(moduleInfo.errors)
            } else Either.Right(moduleInfo)
        }.shouldBeRight()
        .value

fun SemanticModuleInfo.shouldInferredTypesBe(vararg types: String) {
    abstractions.size.shouldBe(types.size)
    abstractions.map {
        it.info.getInferredType(Location.NoProvided)
            .map { type ->
                "${it.name} :: ${type.representation}"
            }
    }.unwrap().shouldBeRight()
        .value.shouldContainExactlyInAnyOrder(types.toList())
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
                "fn :: (KernelUnit -> (Num numeric<Byte>))"
            )
    }
})
