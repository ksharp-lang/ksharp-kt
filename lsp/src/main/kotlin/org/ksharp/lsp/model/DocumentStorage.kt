package org.ksharp.lsp.model

data class DocumentChange(
    val range: Range,
    val content: String
)

class DocumentStorage {
    private val documents = mutableMapOf<String, Document>()

    fun add(uri: String, language: String, content: String) {
        documents[uri] = document(language, content)
    }

    fun remove(uri: String) =
        documents.remove(uri)?.content


    fun update(uri: String, changes: Sequence<DocumentChange>): Boolean =
        documents[uri]?.let { doc ->
            changes.forEach {
                doc.update(it.range, it.content)
            }
            true
        } ?: false

    fun content(uri: String): String? = documents[uri]?.content

    fun <T> withDocumentContent(uri: String, action: (content: String) -> T): T? =
        content(uri)?.let(action)

}
