package org.ksharp.compiler.loader

import java.io.InputStream
import java.io.OutputStream
import java.io.Reader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists

class DirectorySourceLoader(
    private val sources: Path,
    private val binaries: Path
) : SourceLoader {
    override fun binaryLoad(path: String): InputStream? =
        binaries.resolve(path).let {
            if (Files.exists(it)) Files.newInputStream(it)
            else null
        }


    override fun sourceLoad(path: String): Reader? =
        sources.resolve(path).let {
            if (Files.exists(it)) Files.newBufferedReader(it, StandardCharsets.UTF_8)
            else null
        }

    override fun outputStream(path: String, action: (OutputStream) -> Unit) {
        binaries.resolve(path).let {
            try {
                Files.newOutputStream(it).use(action)
            } catch (e: Exception) {
                it.deleteIfExists()
                throw e
            }
        }
    }

}
