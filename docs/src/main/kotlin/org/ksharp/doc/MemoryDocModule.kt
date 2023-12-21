package org.ksharp.doc

import org.ksharp.common.io.BufferWriter

internal data class MemoryDocModule internal constructor(
    val abstractions: List<DocAbstraction>
) : SerializableDocModule {

    private fun abstraction(name: String, container: String = ""): DocAbstraction? =
        abstractions.binarySearch {
            val nameComparator = it.name.compareTo(name)
            if (nameComparator == 0) {
                it.container.compareTo(container)
            } else nameComparator
        }.let {
            if (it < 0) return null
            else abstractions[it]
        }

    override fun representation(name: String, container: String): String? =
        abstraction(name, container)?.representation

    override fun documentation(name: String, container: String): String? =
        abstraction(name, container)?.documentation

    override fun writeTo(buffer: BufferWriter) {
        TODO()
    }
}
