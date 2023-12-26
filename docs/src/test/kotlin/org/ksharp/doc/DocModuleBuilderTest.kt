package org.ksharp.doc

import io.kotest.core.spec.style.StringSpec
import org.ksharp.common.Either
import org.ksharp.common.ErrorOrValue
import org.ksharp.module.prelude.preludeModule
import org.ksharp.parser.ksharp.parseModule
import org.ksharp.semantics.nodes.toCodeModule
import org.ksharp.test.shouldBeRight

private fun String.docModule(): ErrorOrValue<DocModule> =
    this.parseModule("irTest.ks", false)
        .flatMap {
            val codeModule = it.toCodeModule(preludeModule) { _, _ -> null }
            if (codeModule.errors.isNotEmpty()) {
                Either.Left(codeModule.errors.first())
            } else {
                Either.Right(it.toDocModule(codeModule.module))
            }
        }


class DocModuleBuilderTest : StringSpec({
    "Doc on functions" {
        """
            @doc("Sum two values")
            pub sum a b = a + b
        """.trimIndent()
            .docModule()
            .shouldBeRight(
                docModule(
                    emptyList(),
                    emptyList(),
                    listOf(
                        DocAbstraction(
                            "sum/2",
                            "(Add a) -> (Add a) -> (Add a)",
                            "Sum two values"
                        )
                    )
                )
            )
    }
    "Doc on types" {
        """
            @doc("Represent a List of characters")
            type CadenaTexto = List Char
        """.trimIndent()
            .docModule()
            .shouldBeRight(
                docModule(
                    listOf(
                        Type(
                            "CadenaTexto",
                            "(List Char)",
                            "Represent a List of characters"
                        )
                    ),
                    emptyList(),
                    listOf()
                )
            )
    }
})
