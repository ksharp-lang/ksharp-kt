package org.ksharp.lsp.client

import io.kotest.core.spec.style.StringSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import java.net.URI

class ClientWorkSpaceModuleLoaderTest : StringSpec({
    val tmpWorkSpace = tempfile("ksharp-test")
    "Create moduleloader from a URI" {
        ClientWorkspaceModuleLoader.setWorkspaceFolder(
            tmpWorkSpace.toURI()
        )
        ClientWorkspaceModuleLoader.moduleLoader
            .shouldNotBeNull()
    }
    "Create moduleloader from a not existing Path" {
        ClientWorkspaceModuleLoader.setWorkspaceFolder(
            URI("file:///hello")
        )
        ClientWorkspaceModuleLoader.moduleLoader
            .shouldBeNull()
    }
})
