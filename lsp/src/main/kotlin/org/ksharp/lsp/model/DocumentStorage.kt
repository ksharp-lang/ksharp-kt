package org.ksharp.lsp.model

import org.ksharp.lsp.actions.*
import java.util.concurrent.CompletableFuture

data class DocumentChange(
    val range: Range,
    val content: String
)

data class DocumentInstance(
    val document: Document,
    val actions: Actions,
) {
    var state = ActionExecutionState()
        private set

    fun contentUpdated() {
        state = ActionExecutionState()
        actions(state, ParseAction, document.content)
    }

    fun <Payload, Output> executeAction(id: ActionId<Output>, payload: Payload): CompletableFuture<Output> {
        actions(state, id, payload)
        return state.resetActionState(id)
    }
}

class DocumentStorage {
    private val documents = mutableMapOf<String, DocumentInstance>()

    fun add(uri: String, language: String, content: String) {
        documents[uri] = DocumentInstance(
            document(language, content),
            documentActions(uri)
        ).also {
            it.contentUpdated()
        }
    }

    fun remove(uri: String) =
        documents.remove(uri)?.document?.content

    fun update(uri: String, changes: Sequence<DocumentChange>): Boolean =
        documents[uri]?.let { doc ->
            changes.forEach {
                doc.document.update(it.range, it.content)
            }
            doc.contentUpdated()
            true
        } ?: false

    fun content(uri: String): String? = documents[uri]?.document?.content

    fun <T> withDocumentState(
        uri: String,
        action: (state: ActionExecutionState) -> CompletableFuture<T>
    ): CompletableFuture<T> =
        documents[uri]?.let {
            action(it.state)
        } ?: CompletableFuture.failedFuture(RuntimeException("Document $uri not found"))

    fun <Payload, Output> executeAction(
        uri: String,
        id: ActionId<Output>,
        payload: Payload
    ): CompletableFuture<Output> =
        documents[uri]?.executeAction(id, payload) ?: CompletableFuture.failedFuture(
            RuntimeException("Document $uri not found")
        )
}
