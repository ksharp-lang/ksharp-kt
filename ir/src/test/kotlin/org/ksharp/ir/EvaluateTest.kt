package org.ksharp.ir

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.cast

private data class Call(
    val arguments: List<Any>,
    val expectedResult: Any
)

private fun String.evaluateFirstFunction(arguments: List<Any>) =
    toSemanticModuleInfo()
        .toIrModule()
        .symbols
        .first { it is IrFunction }
        .cast<IrFunction>()
        .call(*arguments.toTypedArray())

private fun createSpec(description: String, code: String, expected: Any, vararg arguments: Any) =
    Triple(description, code, Call(arguments.toList(), expected))


class EvaluateTest : StringSpec({
    listOf(
        createSpec("Integer expression", "fn = 10", 10.toLong()),
        createSpec("Decimal expression", "fn = 10.5", 10.5),
        createSpec("Sum expression", "fn = 1 + 2", 3.toLong())
    ).forEach { (description, code, call) ->
        description {
            code.evaluateFirstFunction(call.arguments)
                .shouldBe(call.expectedResult)
        }
    }
})
