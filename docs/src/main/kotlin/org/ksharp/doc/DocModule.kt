package org.ksharp.doc

import org.ksharp.common.io.BufferWriter
import org.ksharp.common.io.newBufferWriter
import java.io.OutputStream

data class DocAbstraction(
    val name: String,
    val container: String = "",
    val representation: String,
    val documentation: String = ""
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


fun SerializableDocModule.writeTo(output: OutputStream) {
    val buffer = newBufferWriter()
    writeTo(buffer)
    buffer.transferTo(output)
}
