package org.ksharp.typesystem.annotations

import org.ksharp.common.*
import org.ksharp.common.io.*

private inline fun BufferWriter.serializeValue(type: String, table: BinaryTable, writeValue: BufferWriter.() -> Unit) =
    newBufferWriter().apply {
        add(0)
        add(table.add(type))
        writeValue()
        set(0, size)
        transferTo(this@serializeValue)
    }

private val serializerWriter = mapOf<String, SerializerWriter<Any>>(
    "String" to SerializerWriter { input, buffer, table ->
        buffer.serializeValue("String", table) {
            add(table.add(input as String))
        }
    },
    "Boolean" to SerializerWriter { input, buffer, table ->
        buffer.serializeValue("Boolean", table) {
            add(if (input as Boolean) 1 else 0)
        }
    },
    "List" to SerializerWriter { input, buffer, table ->
        buffer.serializeValue("List", table) {
            input.cast<List<Any>>().apply {
                add(size)
            }.forEach {
                it.writeTo(this, table)
            }
        }
    },
    "Map" to SerializerWriter { input, buffer, table ->
        buffer.serializeValue("Map", table) {
            input.cast<Map<String, Any>>().apply {
                add(size)
            }.forEach {
                add(table.add(it.key))
                it.value.writeTo(this, table)
            }
        }
    },
    "Annotation" to SerializerWriter { input, buffer, table ->
        buffer.serializeValue("Annotation", table) {
            input.cast<Annotation>().apply {
                add(table.add(this.name))
                attrs.writeTo(this@serializeValue, table)
            }
        }
    }
)

private val serializerReader = mapOf<String, SerializerReader<Any>>(
    "String" to SerializerReader { buffer, table ->
        table[buffer.readInt(0)]
    },
    "Boolean" to SerializerReader { buffer, _ ->
        buffer.readInt(0) == 1
    },
    "List" to SerializerReader { buffer, table ->
        val size = buffer.readInt(0)
        val result = listBuilder<Any>()
        var position = 4
        repeat(size) {
            val valueBuffer = buffer.bufferFrom(position)
            position += valueBuffer.readInt(0)
            result.add(valueBuffer.readValue(table))
        }
        result.build()
    },
    "Map" to SerializerReader { buffer, table ->
        val size = buffer.readInt(0)
        val result = mapBuilder<String, Any>()
        var position = 4
        repeat(size) {
            val key = table[buffer.readInt(position)]
            val valueBuffer = buffer.bufferFrom(position + 4)
            position += valueBuffer.readInt(0) + 4
            result.put(key, valueBuffer.readValue(table))
        }
        result.build()
    },
    "Annotation" to SerializerReader { buffer, table ->
        val name = table[buffer.readInt(0)]
        val attrs = buffer.bufferFrom(4).readValue(table).cast<Map<String, Any>>()
        Annotation(name, attrs)
    }
)

private val Any.serializerName: String
    get() =
        when (this) {
            is String -> "String"
            is Boolean -> "Boolean"
            is Map<*, *> -> "Map"
            is List<*> -> "List"
            is Annotation -> "Annotation"
            else -> throw RuntimeException("Value $this not serializable")
        }

private fun Any.writeTo(buffer: BufferWriter, table: BinaryTable) {
    serializerWriter[serializerName]!!.write(this, buffer, table)
}

private fun BufferView.readValue(table: BinaryTableView): Any {
    val serializer = table[readInt(4)]
    return serializerReader[serializer]!!
        .read(this.bufferFrom(8), table)
}

fun Annotation.writeTo(buffer: BufferWriter, table: BinaryTable) {
    serializerWriter["Annotation"]!!.write(this, buffer, table)
}

fun List<Annotation>.writeTo(buffer: BufferWriter, table: BinaryTable) {
    serializerWriter["List"]!!.write(this, buffer, table)
}

fun BufferView.readAnnotation(table: BinaryTableView): Annotation =
    readValue(table).cast()

fun BufferView.readAnnotations(table: BinaryTableView): List<Annotation> =
    readValue(table).cast()
