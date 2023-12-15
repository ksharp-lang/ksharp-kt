package org.ksharp.typesystem.attributes

import org.ksharp.common.cast
import org.ksharp.common.io.*

val NoAttributes = emptySet<Attribute>()

interface Attribute {
    val name: String
    val writer: SerializerWriter<Attribute>
}

interface AttributeWithValue<T> : Attribute {
    val value: T
}

enum class CommonAttribute(val description: String, override val writer: SerializerWriter<Attribute>) : Attribute {
    //ADD new Attributes at the end of the list
    Native("Symbol implementation is native", CommonAttributeSerializerWriter),
    Public("Symbol accessible in any module", CommonAttributeSerializerWriter),
    Internal("Symbol accessible only in the module where they are defined", CommonAttributeSerializerWriter),
    Impure("Symbol has side effects", CommonAttributeSerializerWriter),
    Pure("Symbol has not side effects", CommonAttributeSerializerWriter),
    Constant("Symbol represent a compile constant value", CommonAttributeSerializerWriter),
    TraitMethod("Symbol is a trait function", CommonAttributeSerializerWriter)
}

interface NameAttribute : AttributeWithValue<Map<String, String>>

interface TargetLanguageAttribute : AttributeWithValue<Set<String>>

private data class NameAttributeImpl(override val name: String = "name") : NameAttribute {

    override lateinit var value: Map<String, String>

    override val writer: SerializerWriter<Attribute>
        get() = NameAttributeSerializerWriter.cast()

}

private data class TargetLanguageAttributeImpl(override val name: String = "targetLanguage") : TargetLanguageAttribute {

    override lateinit var value: Set<String>

    override val writer: SerializerWriter<Attribute>
        get() = TargetLanguageAttributeSerializerWriter.cast()

}

fun nameAttribute(name: Map<String, String>): AttributeWithValue<Map<String, String>> =
    NameAttributeImpl().apply {
        value = name
    }

fun targetLanguageAttribute(targetLanguages: Set<String>): AttributeWithValue<Set<String>> =
    TargetLanguageAttributeImpl().apply {
        value = targetLanguages
    }

internal fun Attribute.writeTo(buffer: BufferWriter, table: BinaryTable) {
    writer.write(this, buffer, table)
}

fun Set<Attribute>.writeTo(buffer: BufferWriter, table: BinaryTable) {
    newBufferWriter().apply {
        if (isEmpty()) {
            add(0)
            set(0, size)
            transferTo(buffer)
        } else {
            add(0)
            add(this@writeTo.size)
            forEach {
                it.writeTo(this, table)
            }
            set(0, size)
            transferTo(buffer)
        }
    }
}

internal fun BufferView.readAttribute(table: BinaryTableView): Attribute {
    val reader = readInt(4)
    val serializer = AttributeSerializerReader
        .entries[reader].cast<AttributeSerializerReader>()
        .reader
    return serializer.read(this, table)
}

fun BufferView.readAttributes(table: BinaryTableView): Set<Attribute> {
    val bufferSize = readInt(0)
    if (bufferSize == 4) return NoAttributes
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

fun Set<Attribute>.merge(attributes: Set<Attribute>): Set<Attribute> {
    if (attributes.isEmpty()) {
        return if (contains(CommonAttribute.Internal)) mutableSetOf<Attribute>().apply {
            addAll(this@merge)
            remove(CommonAttribute.Internal)
        } else this
    }
    if (isEmpty()) return attributes
    return mutableSetOf<Attribute>().apply {
        addAll(this@merge)
        addAll(attributes)
    }
}
