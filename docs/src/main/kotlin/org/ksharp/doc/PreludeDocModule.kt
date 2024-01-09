package org.ksharp.doc

import org.ksharp.common.io.bufferView
import org.ksharp.module.prelude.types.Numeric

private val preludeDocModuleFile: DocModule
    get() =
        String.Companion::class.java.getResourceAsStream("/org/ksharp/module/prelude.ksd")!!
            .use { input ->
                input.bufferView {
                    it.readDocModule()
                }
            }

private val kernelDocModule: DocModule
    get() =
        docModule(
            sequenceOf(
                Numeric.entries.asSequence().map {
                    Type(
                        it.name,
                        it.name,
                        "Signed ${it.size}-bit ${if (it.isInteger) "integer" else "floating point number"}"
                    )
                },
                sequenceOf(
                    Type(
                        "Unit",
                        "()",
                        "The **Unit** type is the terminal object in the category of types"
                    ),
                    Type(
                        "Char",
                        "Char",
                        "Datatype representing a single Unicode character"
                    )
                )
            ).flatten().toList(),
            listOf(
                Trait(
                    "Num",
                    "Allow operations over numeric types",
                    sequenceOf("(+)", "(-)", "(*)", "(/)", "(%)", "(**)")
                        .map {
                            DocAbstraction(
                                it,
                                "a -> a -> a",
                                ""
                            )
                        }.toList(),
                    emptyList()
                )
            ),
            emptyList()
        )

val preludeDocModule: DocModule
    get() {
        val prelude = preludeDocModuleFile
        val kernel = kernelDocModule
        return docModule(
            prelude.types + kernel.types,
            prelude.traits + kernel.traits,
            prelude.abstractions + kernel.abstractions
        )
    }
