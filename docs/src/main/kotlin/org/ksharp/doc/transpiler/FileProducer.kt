package org.ksharp.doc.transpiler

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText

fun interface FileProducer {
    fun write(path: String, content: String)

}

class FileSystemProducer(private val root: Path) : FileProducer {

    override fun write(path: String, content: String) {
        val file = root.resolve(path)
        Files.createDirectories(file.parent)
        file.writeText(content, Charsets.UTF_8)
    }

}
