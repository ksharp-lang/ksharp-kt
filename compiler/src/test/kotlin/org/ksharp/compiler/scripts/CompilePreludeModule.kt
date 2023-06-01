package org.ksharp.compiler.scripts

import io.kotest.core.spec.style.StringSpec
import org.ksharp.compiler.moduleInfo
import org.ksharp.module.bytecode.writeTo
import org.ksharp.module.prelude.kernelModule
import org.ksharp.semantics.nodes.toModuleInfo
import org.ksharp.test.shouldBeRight
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
                it.toModuleInfo()
                    .also { println(it) }.writeTo(FileOutputStream("prelude.ksm"))
            }
    }
})
