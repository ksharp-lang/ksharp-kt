package org.ksharp.lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.*
import org.ksharp.lsp.capabilities.semantic_tokens.kSharpSemanticTokensProvider
import org.ksharp.lsp.client.Client
import org.ksharp.lsp.client.ClientLogger
import org.ksharp.lsp.model.DocumentStorage
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier
import kotlin.system.exitProcess

class KSharpLanguageServer(private val documentStorage: DocumentStorage = DocumentStorage()) : LanguageServer,
    LanguageClientAware {

    override fun initialize(params: InitializeParams): CompletableFuture<InitializeResult> {
        return CompletableFuture.supplyAsync(Supplier {
            InitializeResult().apply {
                capabilities = ServerCapabilities().apply {
                    positionEncoding = "utf-16"
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
        return CompletableFuture.supplyAsync { true }
    }

    override fun exit() {
        exitProcess(0)
    }

    override fun connect(client: LanguageClient) {
        Client.initialize(client)
    }

    override fun getTextDocumentService(): TextDocumentService = KSharpDocumentService(documentStorage)

    override fun getWorkspaceService(): WorkspaceService = KSharpWorkspaceService()

    override fun setTrace(params: SetTraceParams?) {
        ClientLogger.info("Set trace $params.")
    }
}
