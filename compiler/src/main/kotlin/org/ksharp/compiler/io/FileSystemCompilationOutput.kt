package org.ksharp.compiler.io

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText

class FileSystemCompilationOutput(private val root: Path) : CompilerOutput {
    override fun write(path: String, content: String) {
        root.resolve(path)
            .apply {
                Files.createDirectories(parent)
            }
            .writeText(content, StandardCharsets.UTF_8)
    }
}
