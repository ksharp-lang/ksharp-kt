package org.ksharp.lsp

import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.services.TextDocumentService
import org.eclipse.lsp4j.services.WorkspaceService
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

class KSharpLanguageServer : LanguageServer {
    override fun initialize(params: InitializeParams?): CompletableFuture<InitializeResult> =
        CompletableFuture.supplyAsync(Supplier {
            InitializeResult().apply {
                capabilities.apply {
                    setDocumentHighlightProvider(true)
                }
            }
        })

    override fun shutdown(): CompletableFuture<Any> {
        TODO("Not yet implemented")
    }

    override fun exit() {
        TODO("Not yet implemented")
    }

    override fun getTextDocumentService(): TextDocumentService = KSharpDocumentService()

    override fun getWorkspaceService(): WorkspaceService {
        TODO("Not yet implemented")
    }
}
