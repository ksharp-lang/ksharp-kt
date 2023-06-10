package org.ksharp.lsp.model

import org.ksharp.lsp.actions.DocumentActions
import org.ksharp.lsp.actions.documentActions
import java.util.concurrent.CompletableFuture

data class DocumentChange(
    val range: Range,
    val content: String
)

data class DocumentInstance(
    val document: Document,
    val actions: DocumentActions
)

class DocumentStorage {
    private val documents = mutableMapOf<String, DocumentInstance>()

    fun add(uri: String, language: String, content: String) {
        documents[uri] = DocumentInstance(
            document(language, content),
            documentActions(uri)
        )
    }

    fun remove(uri: String) =
        documents.remove(uri)?.document?.content

    fun update(uri: String, changes: Sequence<DocumentChange>): Boolean =
        documents[uri]?.let { doc ->
            changes.forEach {
                doc.document.update(it.range, it.content)
            }
            true
        } ?: false

    fun content(uri: String): String? = documents[uri]?.document?.content

    fun <T> withDocumentContent(
        uri: String,
        action: (actions: DocumentActions, content: String) -> CompletableFuture<T>
    ): CompletableFuture<T> =
        documents[uri]?.let {
            action(it.actions, it.document.content)
        } ?: CompletableFuture.failedFuture(RuntimeException("Document $uri not found"))

}
