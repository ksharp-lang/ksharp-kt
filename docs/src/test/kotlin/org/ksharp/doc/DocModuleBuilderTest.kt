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
    "No doc annotation" {
        """
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
                            "(Num a) -> (Num a) -> (Num a)",
                            ""
                        )
                    )
                )
            )
    }
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
                            "(Num a) -> (Num a) -> (Num a)",
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
                    emptyList()
                )
            )
    }
    "Doc on traits" {
        """
            @doc("Math operations")
            trait Math a =
                @doc("Sum two values")
                sum :: a -> a -> a
                
                @doc("Subtract two values")
                sub :: a -> a -> a
                
                @doc("Pi value")
                pi :: () -> a
                
                @doc("Euler number")
                euler :: Unit -> a
        """.trimIndent()
            .docModule()
            .shouldBeRight(
                docModule(
                    emptyList(),
                    listOf(
                        Trait(
                            "Math",
                            "Math operations",
                            listOf(
                                DocAbstraction(
                                    "sum/2",
                                    "sum :: a -> a -> a",
                                    "Sum two values"
                                ),
                                DocAbstraction(
                                    "sub/2",
                                    "sub :: a -> a -> a",
                                    "Subtract two values"
                                ),
                                DocAbstraction(
                                    "pi/0",
                                    "pi :: Unit -> a",
                                    "Pi value"
                                ),
                                DocAbstraction(
                                    "euler/0",
                                    "euler :: Unit -> a",
                                    "Euler number"
                                )
                            ),
                            emptyList()
                        )
                    ),
                    emptyList()
                )
            )
    }
})
