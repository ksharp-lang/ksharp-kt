package org.ksharp.lsp.client

import org.ksharp.compiler.loader.DirectorySourceLoader
import org.ksharp.compiler.loader.ModuleLoader
import org.ksharp.module.prelude.preludeModule
import java.net.URI
import java.nio.file.Files
import kotlin.io.path.name
import kotlin.io.path.toPath

object ClientWorkspaceModuleLoader {
    var moduleLoader: ModuleLoader? = null
        private set

    fun setWorkspaceFolder(uri: URI) {
        val sources = uri.toPath().toAbsolutePath()
        val folderName = sources.name
        val sourceLoader = DirectorySourceLoader(
            sources,
            Files.createTempDirectory("ksharp-$folderName")
        )
        moduleLoader = ModuleLoader(sourceLoader, preludeModule)
    }
}
