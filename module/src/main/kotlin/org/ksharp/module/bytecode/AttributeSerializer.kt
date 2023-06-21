package org.ksharp.module.bytecode

import org.ksharp.common.cast
import org.ksharp.common.io.*
import org.ksharp.common.mapBuilder
import org.ksharp.common.put
import org.ksharp.module.Attribute
import org.ksharp.module.AttributeWithValue
import org.ksharp.module.nameAttribute

class EnumAttributeSerializerReader : SerializerReader<Attribute> {
    override fun read(buffer: BufferView, table: BinaryTableView): Attribute {
        val clazz = Class.forName(table[buffer.readInt(8)]).cast<Class<Enum<*>>>()
        return clazz.enumConstants[buffer.readInt(12)].cast()
    }

}

class NameAttributeSerializerReader : SerializerReader<Attribute> {
    override fun read(buffer: BufferView, table: BinaryTableView): Attribute {
        return nameAttribute(buffer.bufferFrom(8).readMapOfStrings(table))
    }

}

val EnumAttributeSerializerWriter =
    SerializerWriter<Attribute> { input, buffer, table ->
        buffer.add(16) //0
        buffer.add(table.add(EnumAttributeSerializerReader::class.java.name)) //4
        buffer.add(table.add(input.javaClass.name)) //8
        buffer.add((input as Enum<*>).ordinal) // 12
    }

val NameAttributeSerializerWriter = SerializerWriter<AttributeWithValue<Map<String, String>>> { input, buffer, table ->
    newBufferWriter().apply {
        add(0)
        add(table.add(NameAttributeSerializerReader::class.java.name))
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

private fun Map<String, String>.writeTo(buffer: BufferWriter, table: BinaryTable) {
    asSequence()
        .map { entry -> entry.key to entry.value }
        .writeTo(size, buffer, table)
}

private fun BufferView.readMapOfStrings(table: BinaryTableView): Map<String, String> {
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
