package org.ksharp.lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.TextDocumentService
import java.util.concurrent.CompletableFuture

class KSharpDocumentService : TextDocumentService {
    override fun didOpen(params: DidOpenTextDocumentParams?) {
        println(params)
    }

    override fun didChange(params: DidChangeTextDocumentParams?) {
        println(params)
    }

    override fun didClose(params: DidCloseTextDocumentParams?) {
        println(params)
    }

    override fun didSave(params: DidSaveTextDocumentParams?) {
        println(params)
    }

    override fun documentHighlight(params: DocumentHighlightParams?): CompletableFuture<MutableList<out DocumentHighlight>> {
        return super.documentHighlight(params)
    }
}
