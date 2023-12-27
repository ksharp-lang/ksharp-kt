package org.ksharp.doc

import org.ksharp.common.io.bufferView

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
            listOf(
                Type(
                    "Unit",
                    "()",
                    "The **Unit** type is the terminal object in the category of types"
                ),
                Type(
                    "Char",
                    "char<Char>",
                    "Datatype representing a single Unicode character"
                ),
                Type(
                    "Byte",
                    "(Num numeric<Byte>)",
                    "Signed 8-bit integer"
                ),
                Type(
                    "Short",
                    "(Num numeric<Short>)",
                    "Signed 16-bit integer"
                ),
                Type(
                    "Int",
                    "(Num numeric<Int>)",
                    "Signed 32-bit integer"
                ),
                Type(
                    "Long",
                    "(Num numeric<Long>)",
                    "Signed 64-bit integer. *This is the default type for integer numbers*"
                ),
                Type(
                    "BigInt",
                    "(Num numeric<BigInt>)",
                    "Signed arbitrary-precision integer"
                ),
                Type(
                    "Float",
                    "(Num numeric<Float>)",
                    "Signed 32-bit floating point number."
                ),
                Type(
                    "Double",
                    "(Num numeric<Double>)",
                    "Signed 64-bit floating point number. *This is the default type for integer numbers*"
                ),
                Type(
                    "BigDecimal",
                    "(Num numeric<BigDecimal>)",
                    "Signed arbitrary-precision floating point number"
                )
            ), emptyList(), emptyList()
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
