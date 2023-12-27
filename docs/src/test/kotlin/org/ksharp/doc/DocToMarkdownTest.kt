package org.ksharp.doc

import io.kotest.core.spec.style.StringSpec
import org.ksharp.doc.transpiler.DocusaurusTranspilerPlugin
import org.ksharp.doc.transpiler.transpile
import java.io.File
import java.nio.file.Files

class DocToMarkdownTest : StringSpec({
    "prelude docModule to markdown" {
        val prelude = preludeDocModule
        val root = File("docOutput").absoluteFile.toPath()
        Files.createDirectories(root)
        prelude.transpile("prelude", DocusaurusTranspilerPlugin(root))
    }
})
