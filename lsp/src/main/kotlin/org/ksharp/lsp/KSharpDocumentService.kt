package org.ksharp.lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.TextDocumentService
import org.ksharp.lsp.capabilities.semantic_tokens.calculateSemanticTokens
import org.ksharp.lsp.model.DocumentChange
import org.ksharp.lsp.model.DocumentStorage
import org.ksharp.lsp.model.Range
import java.util.concurrent.CompletableFuture

class KSharpDocumentService(private val documentStorage: DocumentStorage) : TextDocumentService {

    override fun didOpen(params: DidOpenTextDocumentParams) {
        with(params.textDocument) {
            documentStorage.add(uri, languageId, text)
        }
    }

    override fun didChange(params: DidChangeTextDocumentParams) {
        documentStorage.update(params.textDocument.uri,
            params.contentChanges.asSequence().map { event ->
                val range = event.range
                DocumentChange(
                    Range(
                        range.start.let {
                            it.line to it.character
                        },
                        range.end.let {
                            it.line to it.character
                        }
                    ),
                    event.text
                )
            })
    }

    override fun didClose(params: DidCloseTextDocumentParams) {
        documentStorage.remove(params.textDocument.uri)
    }

    override fun didSave(params: DidSaveTextDocumentParams) {
        //not required
    }

    override fun semanticTokensFull(params: SemanticTokensParams): CompletableFuture<SemanticTokens> =
        documentStorage.withDocumentContent(params.textDocument.uri, ::calculateSemanticTokens)

}
