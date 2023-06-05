package org.ksharp.lsp

import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.DidChangeWatchedFilesParams
import org.eclipse.lsp4j.services.WorkspaceService
import org.ksharp.lsp.client.ClientLogger

class KSharpWorkspaceService : WorkspaceService {
    override fun didChangeConfiguration(params: DidChangeConfigurationParams) {
        ClientLogger.info("didChangeConfiguration: $params")
    }

    override fun didChangeWatchedFiles(params: DidChangeWatchedFilesParams) {
        ClientLogger.info("didChangeWatchedFiles: $params")
    }
}
