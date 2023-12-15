package org.ksharp.typesystem.attributes

import org.ksharp.common.cast
import org.ksharp.common.io.*
import org.ksharp.common.mapBuilder
import org.ksharp.common.put

enum class AttributeSerializerReader(val reader: SerializerReader<Attribute>) {
    //ADD new Serializers at the end of the list
    Common(CommonAttributeSerializerReader()),
    Name(NameAttributeSerializerReader()),
    TargetLanguage(TargetLanguageAttributeSerializerReader()),
}

class CommonAttributeSerializerReader : SerializerReader<Attribute> {
    override fun read(buffer: BufferView, table: BinaryTableView): Attribute {
        val ordinal = buffer.readInt(8)
        return CommonAttribute.entries[ordinal].cast()
    }

}

class NameAttributeSerializerReader : SerializerReader<Attribute> {
    override fun read(buffer: BufferView, table: BinaryTableView): Attribute {
        return nameAttribute(buffer.bufferFrom(8).readMapOfStrings(table))
    }

}

class TargetLanguageAttributeSerializerReader : SerializerReader<Attribute> {
    override fun read(buffer: BufferView, table: BinaryTableView): Attribute {
        return targetLanguageAttribute(buffer.bufferFrom(8).readSetOfStrings(table))
    }
}


val CommonAttributeSerializerWriter =
    SerializerWriter<Attribute> { input, buffer, _ ->
        buffer.add(12) //0
        buffer.add(AttributeSerializerReader.Common.ordinal) //4
        buffer.add((input as Enum<*>).ordinal) // 8
    }

val NameAttributeSerializerWriter = SerializerWriter<AttributeWithValue<Map<String, String>>> { input, buffer, table ->
    newBufferWriter().apply {
        add(0)
        add(AttributeSerializerReader.Name.ordinal) //4
        input.value.writeTo(this, table)
        set(0, size)
        transferTo(buffer)
    }
}

val TargetLanguageAttributeSerializerWriter =
    SerializerWriter<AttributeWithValue<Set<String>>> { input, buffer, table ->
        newBufferWriter().apply {
            add(0)
            add(AttributeSerializerReader.TargetLanguage.ordinal) //4
            input.value.writeTo(this, table)
            set(0, size)
            transferTo(buffer)
        }
    }


private fun Sequence<Pair<String, String>>.writeTo(size: Int, buffer: BufferWriter, table: BinaryTable) {
    buffer.add(size)
    forEach { (name, value) ->
        buffer.add(table.add(name))
        buffer.add(table.add(value))
    }
}

fun Map<String, String>.writeTo(buffer: BufferWriter, table: BinaryTable) {
    asSequence()
        .map { entry -> entry.key to entry.value }
        .writeTo(size, buffer, table)
}

private fun Set<String>.writeTo(buffer: BufferWriter, table: BinaryTable) {
    buffer.add(size)
    forEach { value ->
        buffer.add(table.add(value))
    }
}

fun BufferView.readMapOfStrings(table: BinaryTableView): Map<String, String> {
    val paramsSize = readInt(0)
    val result = mapBuilder<String, String>()
    var position = 4
    repeat(paramsSize) {
        val key = table[readInt(position)]
        position += 4
        val value = table[readInt(position)]
        position += 4
        result.put(
            key,
            value
        )
    }
    return result.build()
}

private fun BufferView.readSetOfStrings(table: BinaryTableView): Set<String> {
    val paramsSize = readInt(0)
    val result = mutableSetOf<String>()
    var position = 4
    repeat(paramsSize) {
        val value = table[readInt(position)]
        position += 4
        result.add(value)
    }
    return result
}
