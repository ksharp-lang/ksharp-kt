package org.ksharp.doc

import org.ksharp.common.io.BufferView
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

    private fun BufferWriter.write(value: String) {
        if (value.isEmpty()) add(0)
        else value.toByteArray(Charsets.UTF_8).let {
            add(it.size)
            add(it)
        }
    }

    override fun writeTo(buffer: BufferWriter) {
        buffer.add(abstractions.size)
        abstractions.forEach {
            buffer.write(it.name)
            buffer.write(it.container)
            buffer.write(it.representation)
            buffer.write(it.documentation)
        }
    }
}

private fun readString(buffer: BufferView, offset: Int): Pair<String, Int> {
    val size = buffer.readInt(offset)
    return (offset + 4).let {
        if (size == 0) return "" to it
        val string = buffer.readString(it, size)
        string to it + size
    }
}

fun BufferView.readDocModule(): SerializableDocModule {
    val size = readInt(0)
    val abstractions = mutableListOf<DocAbstraction>()
    var offset = 4
    repeat(size) {
        val (name, nameOffset) = readString(this, offset)
        val (container, containerOffset) = readString(this, nameOffset)
        val (representation, representationOffset) = readString(this, containerOffset)
        val (documentation, documentationOffset) = readString(this, representationOffset)
        offset = documentationOffset
        abstractions.add(
            DocAbstraction(
                name,
                container,
                representation,
                documentation
            )
        )
    }
    return MemoryDocModule(abstractions)
}
