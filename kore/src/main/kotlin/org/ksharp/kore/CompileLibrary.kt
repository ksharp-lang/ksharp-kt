package org.ksharp.kore

import org.ksharp.compiler.loader.DirectorySourceLoader
import org.ksharp.compiler.loader.ModuleLoader
import org.ksharp.module.prelude.preludeModule
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.relativeTo

val Path.ksFiles: Sequence<Path>
    get() =
        sequenceOf(
            listDirectoryEntries("**.ks").asSequence(),
            listDirectoryEntries("*")
                .asSequence()
                .filter {
                    Files.isDirectory(it)
                }.map {
                    it.ksFiles
                }.flatten()
        ).flatten()

fun main() {
    val root = File("").absoluteFile.toPath()
    val sources = root.resolve("src/main/resources/")
    val sourcesLoader = DirectorySourceLoader(
        sources.resolve("sources"),
        sources
    )
    val moduleLoader = ModuleLoader(sourcesLoader, preludeModule)
    val sourcesDir = sources.resolve("sources")
    sourcesDir.ksFiles.forEach { p ->
        val moduleName = p.relativeTo(sourcesDir).toString().let {
            it.substring(0, it.length - ".ks".length)
        }
        moduleLoader.load(moduleName, "")
    }
}
