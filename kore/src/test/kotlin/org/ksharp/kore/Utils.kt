package org.ksharp.kore

import org.ksharp.compiler.loader.ModuleLoader
import org.ksharp.compiler.loader.SourceLoader
import org.ksharp.module.prelude.preludeModule
import java.io.*

private fun sources(code: String): SourceLoader {
    val output = ByteArrayOutputStream()
    return object : SourceLoader {
        override fun binaryLoad(path: String): InputStream? =
            if (path == "irTest.ksc") ByteArrayInputStream(output.toByteArray())
            else if (path.startsWith("irTest.")) null
            else javaClass.getResourceAsStream("/$path")

        override fun sourceLoad(path: String): Reader? =
            if (path == "irTest.ks") {
                code.reader()
            } else null

        override fun outputStream(path: String, action: (OutputStream) -> Unit) {
            if (path == "irTest.ksc") {
                action(output)
                return
            }
        }

    }
}

fun String.evaluateFunction(function: String, vararg arguments: Any) =
    ModuleLoader(sources(this), preludeModule)
        .load("irTest", "")
        .map {
            it.executable
                .execute(function, *arguments)
        }
