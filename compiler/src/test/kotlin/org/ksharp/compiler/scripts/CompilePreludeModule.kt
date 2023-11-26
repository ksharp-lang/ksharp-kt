package org.ksharp.compiler.scripts

import io.kotest.core.spec.style.StringSpec
import org.ksharp.compiler.moduleInfo
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
        val moduleInfo = moduleReader.moduleInfo("prelude.ks", kernelModule)
        moduleInfo
            .shouldBeRight()
            .map {
                it.module
                    .also { m ->
                        m.functions.keys.onEach(::println)
                        println("=== Traits ===")
                        m.typeSystem.asSequence().forEach { (_, type) ->
                            if (type is TraitType) {
                                println(type.name)
                            }
                        }
                        println("=== Impls ===")
                        m.impls.onEach(::println)
                    }
                    .writeTo(FileOutputStream("prelude.ksm"))
            }

    }
})
