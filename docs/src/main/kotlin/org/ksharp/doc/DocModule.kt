package org.ksharp.doc

import org.ksharp.common.io.BufferWriter

data class DocAbstraction(
    val name: String,
    val container: String,
    val representation: String,
    val documentation: String?,
)

interface DocModule {
    fun representation(name: String, container: String = ""): String?

    fun documentation(name: String, container: String = ""): String?

}

interface SerializableDocModule : DocModule {
    fun writeTo(buffer: BufferWriter)

}

fun docModule(abstractions: List<DocAbstraction>): SerializableDocModule =
    MemoryDocModule(
        abstractions.sortedBy {
            "${it.name} :: ${it.container}"
        })
