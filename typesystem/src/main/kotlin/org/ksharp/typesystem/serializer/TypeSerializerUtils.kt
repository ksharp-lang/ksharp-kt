package org.ksharp.typesystem.serializer

import org.ksharp.common.add
import org.ksharp.common.io.BinaryTable
import org.ksharp.common.io.BinaryTableView
import org.ksharp.common.io.BufferView
import org.ksharp.common.io.BufferWriter
import org.ksharp.common.listBuilder
import org.ksharp.common.mapBuilder
import org.ksharp.common.put
import org.ksharp.typesystem.types.Type

fun BufferView.readListOfTypes(tableView: BinaryTableView): List<Type> {
    val paramsSize = readInt(0)
    val result = listBuilder<Type>()
    var position = 4
    repeat(paramsSize) {
        val typeBuffer = bufferFrom(position)
        position = readInt(position) - offset
        result.add(readTypeFrom(typeBuffer, tableView))
    }
    return result.build()
}

fun BufferView.readMapOfTypes(table: BinaryTableView): Map<String, Type> {
    val paramsSize = readInt(0)
    val types = mapBuilder<String, Type>()
    var position = 4
    repeat(paramsSize) {
        val typeBuffer = bufferFrom(position)
        val key = table[typeBuffer.readInt(0)]
        position = typeBuffer.readInt(4) - offset
        types.put(
            key,
            readTypeFrom(typeBuffer.bufferFrom(4), table)
        )
    }
    return types.build()
}

fun List<Type>.writeTo(buffer: BufferWriter, table: BinaryTable) {
    buffer.add(size)
    forEach {
        it.writeTo(buffer, table)
    }
}

fun Map<String, Type>.writeTo(buffer: BufferWriter, table: BinaryTable) {
    asSequence().map { entry -> entry.key to entry.value }
        .writeTo(size, buffer, table)
}

fun Sequence<Pair<String, Type>>.writeTo(size: Int, buffer: BufferWriter, table: BinaryTable) {
    buffer.add(size)
    forEach { (name, type) ->
        buffer.add(table.add(name))
        type.writeTo(buffer, table)
    }
}