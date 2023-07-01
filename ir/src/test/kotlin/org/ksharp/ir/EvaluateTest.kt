package org.ksharp.ir

import com.oracle.truffle.api.strings.TruffleString
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
        .first
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
        createSpec("Character expression", "fn = 'a'", 'a'),
        createSpec(
            "String expression", "fn = \"Hello World\"",
            TruffleString.FromJavaStringNode.create().execute("Hello World", TruffleString.Encoding.UTF_16)
        ),
        createSpec(
            "List expression", "fn = [1, 2, 3]",
            listOf(1.toLong(), 2.toLong(), 3.toLong())
        ),
        createSpec(
            "Set expression", "fn = #[1, 2, 3]",
            setOf(1.toLong(), 2.toLong(), 3.toLong())
        ),
        createSpec(
            "Map expression", "fn = {1 : 2, 2: 4, 3: 6}",
            mapOf(1.toLong() to 2.toLong(), 2.toLong() to 4.toLong(), 3.toLong() to 6.toLong())
        ),
        createSpec("Sum expression", "fn = 1 + 2", 3.toLong()),
        createSpec("Sub expression", "fn = 1 - 2", (-1).toLong()),
        createSpec("Mul expression", "fn = 2 * 3", 6.toLong()),
        createSpec("Div expression", "fn = 6 / 2", 3.toLong()),
        createSpec("Pow expression", "fn = 2 ** 3", 8.toLong()),
        createSpec("Mod expression", "fn = 7 % 2", 1.toLong()),
        createSpec("If then expression", "fn = if True then 1 else 2", 1.toLong()),
        createSpec("If else expression", "fn = if False then 1 else 2", 2.toLong()),
        createSpec(
            "Arg access expression", """
            fn :: Char -> Char
            fn a = a
        """.trimIndent(), 'a', 'a'
        ),
        createSpec(
            "Call expression", """
            fn = sum 10 20
            sum a b = a + b
        """.trimIndent(), 30.toLong()
        ),
    ).forEach { (description, code, call) ->
        description {
            code.evaluateFirstFunction(call.arguments)
                .shouldBe(call.expectedResult)
        }
    }
})
