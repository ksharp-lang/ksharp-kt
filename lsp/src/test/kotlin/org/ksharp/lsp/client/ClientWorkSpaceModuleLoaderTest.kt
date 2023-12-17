package org.ksharp.lsp.client

import io.kotest.core.spec.style.StringSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.nulls.shouldNotBeNull

class ClientWorkSpaceModuleLoaderTest : StringSpec({
    val tmpWorkSpace = tempfile("ksharp-test")
    "Create moduleloader from a URI" {
        ClientWorkspaceModuleLoader.setWorkspaceFolder(
            tmpWorkSpace.toURI()
        )
        ClientWorkspaceModuleLoader.moduleLoader
            .shouldNotBeNull()
    }
})
