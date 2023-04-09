package org.ksharp.typesystem.serializer

import org.ksharp.common.add
import org.ksharp.common.io.BinaryTable
import org.ksharp.common.io.BinaryTableView
import org.ksharp.common.io.BufferView
import org.ksharp.common.io.BufferWriter
import org.ksharp.common.listBuilder
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

fun List<Type>.writeTo(buffer: BufferWriter, table: BinaryTable) {
    buffer.add(size)
    forEach {
        it.writeTo(buffer, table)
    }
}