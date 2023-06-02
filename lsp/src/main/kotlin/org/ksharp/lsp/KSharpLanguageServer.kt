package org.ksharp.lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.*
import org.ksharp.lsp.capabilities.semantic_tokens.kSharpSemanticTokensProvider
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

class KSharpLanguageServer : LanguageServer, LanguageClientAware {

    override fun initialize(params: InitializeParams?): CompletableFuture<InitializeResult> {
        ClientLogger.info("KSharp server: initialize $params")
        return CompletableFuture.supplyAsync(Supplier {
            InitializeResult().apply {
                capabilities = ServerCapabilities().apply {
                    setTextDocumentSync(TextDocumentSyncOptions().apply {
                        openClose = true
                        change = TextDocumentSyncKind.Incremental
                    })
                    semanticTokensProvider = kSharpSemanticTokensProvider
                }
            }
        })
    }

    override fun shutdown(): CompletableFuture<Any> {
        ClientLogger.info("KSharp server: shutdown")
        return CompletableFuture.supplyAsync { true }
    }

    override fun exit() {
        ClientLogger.info("KSharp server: exit")
    }

    override fun connect(client: LanguageClient) {
        ClientLogger.initialize(client)
    }

    override fun getTextDocumentService(): TextDocumentService = KSharpDocumentService()

    override fun getWorkspaceService(): WorkspaceService = KSharpWorkspaceService()
}
