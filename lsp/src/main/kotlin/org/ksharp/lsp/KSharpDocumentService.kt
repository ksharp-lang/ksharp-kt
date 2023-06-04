package org.ksharp.lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.TextDocumentService
import org.ksharp.lsp.client.ClientLogger
import java.util.concurrent.CompletableFuture

class KSharpDocumentService : TextDocumentService {
    override fun didOpen(params: DidOpenTextDocumentParams?) {
        ClientLogger.info("didOpen: $params")
    }

    override fun didChange(params: DidChangeTextDocumentParams?) {
        ClientLogger.info("didChange: $params")
    }

    override fun didClose(params: DidCloseTextDocumentParams?) {
        ClientLogger.info("didClose: $params")
    }

    override fun didSave(params: DidSaveTextDocumentParams?) {
        ClientLogger.info("didSave: $params")
    }

    override fun semanticTokensFull(params: SemanticTokensParams?): CompletableFuture<SemanticTokens> {
        ClientLogger.info("semanticTokensFull: $params")
        return CompletableFuture.supplyAsync {
            SemanticTokens().apply {
                data = listOf()
            }
        }
    }

}
