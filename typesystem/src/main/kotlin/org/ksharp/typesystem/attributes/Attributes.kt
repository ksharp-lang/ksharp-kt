package org.ksharp.typesystem.attributes

import org.ksharp.common.cast
import org.ksharp.common.io.*

interface Attribute {
    val name: String
    val writer: SerializerWriter<Attribute>
}

interface AttributeWithValue<T> : Attribute {
    val value: T
}

enum class CommonAttribute(val description: String, override val writer: SerializerWriter<Attribute>) : Attribute {
    Native("Symbol implementation is native", EnumAttributeSerializerWriter),
    Public("Symbol accessible in any module", EnumAttributeSerializerWriter),
    Internal("Symbol accessible only in the module where they are defined", EnumAttributeSerializerWriter),
    Impure("Symbol has side effects", EnumAttributeSerializerWriter),
    Constant("Symbol represent a compile constant value", EnumAttributeSerializerWriter)
}

interface NameAttribute : AttributeWithValue<Map<String, String>>

private data class NameAttributeImpl(override val name: String = "name") : NameAttribute {

    override lateinit var value: Map<String, String>

    override val writer: SerializerWriter<Attribute>
        get() = NameAttributeSerializerWriter.cast()

}

fun nameAttribute(name: Map<String, String>): AttributeWithValue<Map<String, String>> =
    NameAttributeImpl().apply {
        value = name
    }

internal fun Attribute.writeTo(buffer: BufferWriter, table: BinaryTable) {
    writer.write(this, buffer, table)
}

fun Set<Attribute>.writeTo(buffer: BufferWriter, table: BinaryTable) {
    newBufferWriter().apply {
        add(0)
        add(this@writeTo.size)
        forEach {
            it.writeTo(this, table)
        }
        set(0, size)
        transferTo(buffer)
    }

}

internal fun BufferView.readAttribute(table: BinaryTableView): Attribute {
    val reader = table[readInt(4)]
    val serializer = Class.forName(reader)
        .getDeclaredConstructor()
        .newInstance()
        .cast<SerializerReader<Attribute>>()
    return serializer.read(this, table)
}

fun BufferView.readAttributes(table: BinaryTableView): Set<Attribute> {
    val size = readInt(4)
    var position = 8
    val result = mutableSetOf<Attribute>()
    repeat(size) {
        val itemBuffer = bufferFrom(position)
        position += itemBuffer.readInt(0)
        result.add(itemBuffer.readAttribute(table))
    }
    return result
}
