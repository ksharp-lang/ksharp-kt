package org.ksharp.doc

import org.ksharp.common.io.BufferView
import org.ksharp.common.io.BufferWriter
import org.ksharp.common.io.newBufferWriter
import java.io.OutputStream

private fun readString(buffer: BufferView, offset: Int): Pair<String, Int> {
    val size = buffer.readInt(offset)
    return (offset + 4).let {
        if (size == 0) return "" to it
        val string = buffer.readString(it, size)
        string to it + size
    }
}

private fun BufferView.readAbstractions(offset: Int): Pair<List<DocAbstraction>, Int> {
    val size = readInt(offset)
    var abstractionOffset = offset + 4
    val abstractions = mutableListOf<DocAbstraction>()
    repeat(size) {
        val (name, nameOffset) = readString(this, abstractionOffset)
        val (representation, representationOffset) = readString(this, nameOffset)
        val (documentation, documentationOffset) = readString(this, representationOffset)
        abstractionOffset = documentationOffset
        abstractions.add(
            DocAbstraction(
                name,
                representation,
                documentation
            )
        )
    }
    return abstractions to abstractionOffset
}

private fun BufferView.readImpls(offset: Int): Pair<List<String>, Int> {
    val size = readInt(offset)
    var implsOffset = offset + 4
    val impls = mutableListOf<String>()
    repeat(size) {
        val (name, nameOffset) = readString(this, implsOffset)
        implsOffset = nameOffset
        impls.add(name)
    }
    return impls to implsOffset
}

private fun BufferWriter.writeString(value: String) {
    if (value.isEmpty()) add(0)
    else value.toByteArray(Charsets.UTF_8).let {
        add(it.size)
        add(it)
    }
}

@JvmName("writeAbstractionsTo")
private fun List<DocAbstraction>.writeTo(buffer: BufferWriter) {
    buffer.add(size)
    forEach {
        buffer.writeString(it.name)
        buffer.writeString(it.representation)
        buffer.writeString(it.documentation)
    }
}

@JvmName("writeImplsTo")
private fun List<String>.writeTo(buffer: BufferWriter) {
    buffer.add(size)
    forEach {
        buffer.writeString(it)
    }
}

private fun DocModule.writeTo(buffer: BufferWriter) {
    buffer.add(this.types.size)
    this.types.forEach {
        buffer.writeString(it.name)
        buffer.writeString(it.representation)
        buffer.writeString(it.documentation)
    }
    buffer.add(this.traits.size)
    this.traits.forEach {
        buffer.writeString(it.name)
        buffer.writeString(it.documentation)
        it.abstractions.writeTo(buffer)
        it.impls.writeTo(buffer)
    }
    abstractions.writeTo(buffer)
}

fun DocModule.writeTo(output: OutputStream) {
    val buffer = newBufferWriter()
    writeTo(buffer)
    buffer.transferTo(output)
}

fun BufferView.readDocModule(): DocModule {
    val typeSize = readInt(0)
    var offset = 4
    val types = mutableListOf<Type>()
    repeat(typeSize) {
        val (name, nameOffset) = readString(this, offset)
        val (representation, representationOffset) = readString(this, nameOffset)
        val (documentation, documentationOffset) = readString(this, representationOffset)
        offset = documentationOffset
        types.add(Type(name, representation, documentation))
    }

    val traitsSize = readInt(offset)
    offset += 4
    val traits = mutableListOf<Trait>()
    repeat(traitsSize) {
        val (name, nameOffset) = readString(this, offset)
        val (documentation, documentationOffset) = readString(this, nameOffset)
        val (abstractions, abstractionOffset) = readAbstractions(documentationOffset)
        val (impls, implsOffset) = readImpls(abstractionOffset)
        offset = implsOffset
        traits.add(Trait(name, documentation, abstractions, impls))
    }

    val (abstractions, _) = readAbstractions(offset)
    return MemoryDocModule(types, traits, abstractions)
}
