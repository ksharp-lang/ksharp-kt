package org.ksharp.lsp.model

import org.ksharp.lsp.actions.ActionExecutionState
import org.ksharp.lsp.actions.Actions
import org.ksharp.lsp.actions.ParseAction
import org.ksharp.lsp.actions.documentActions
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

    fun executeActions() {
        state = ActionExecutionState()
        actions(state, ParseAction, document.content)
    }
}

class DocumentStorage {
    private val documents = mutableMapOf<String, DocumentInstance>()

    fun add(uri: String, language: String, content: String) {
        documents[uri] = DocumentInstance(
            document(language, content),
            documentActions(uri)
        ).also {
            it.executeActions()
        }
    }

    fun remove(uri: String) =
        documents.remove(uri)?.document?.content

    fun update(uri: String, changes: Sequence<DocumentChange>): Boolean =
        documents[uri]?.let { doc ->
            changes.forEach {
                doc.document.update(it.range, it.content)
            }
            doc.executeActions()
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

}
