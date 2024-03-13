package org.ksharp.compiler.scripts

import io.kotest.core.spec.style.StringSpec
import org.ksharp.compiler.codeModule
import org.ksharp.compiler.docModule
import org.ksharp.compiler.moduleNode
import org.ksharp.doc.writeTo
import org.ksharp.module.bytecode.writeTo
import org.ksharp.module.prelude.kernelModule
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.types.TraitType
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

class CompilePreludeModule : StringSpec({
    "Create the prelude module bytecode" {
        val moduleReader = String.Companion::class.java
            .getResourceAsStream("/org/ksharp/module/prelude.ks")!!
            .bufferedReader(StandardCharsets.UTF_8)
        val moduleInfo = moduleReader.moduleNode("")
        val codeModule = moduleInfo.codeModule(kernelModule)
        codeModule
            .mapLeft {
                it.forEach { e ->
                    println(e.representation)
                }
                it
            }
            .shouldBeRight()
            .map {
                it.module
                    .also { m ->
                        m.functions.keys.onEach(::println)
                        println("=== Traits ===")
                        m.typeSystem.asSequence().forEach { (_, type) ->
                            if (type is TraitType) {
                                println("${type.module}${type.name}")
                            }
                        }
                        println("=== Impls ===")
                        m.impls.onEach(::println)
                    }
                    .writeTo(FileOutputStream("prelude.ksm"))

                moduleInfo.docModule(it.module)
                    .map { docModule ->
                        docModule.writeTo(FileOutputStream("prelude.ksd"))
                    }
            }
    }
})
