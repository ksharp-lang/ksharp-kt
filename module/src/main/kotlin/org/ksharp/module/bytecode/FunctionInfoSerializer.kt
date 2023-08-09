package org.ksharp.module.bytecode

import org.ksharp.common.io.*
import org.ksharp.common.mapBuilder
import org.ksharp.common.put
import org.ksharp.module.FunctionInfo
import org.ksharp.module.FunctionInfoImpl
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo
import org.ksharp.typesystem.serializer.readListOfTypes
import org.ksharp.typesystem.serializer.writeTo

fun FunctionInfo.writeTo(buffer: BufferWriter, table: BinaryTable) {
    newBufferWriter().apply {
        add(0) // 0
        attributes.writeTo(this, table) // 4
        add(table.add(name)) // 8
        types.writeTo(this, table) //12
        set(0, size)
        transferTo(buffer)
    }
}

fun BufferView.readFunctionInfo(table: BinaryTableView): FunctionInfo {
    val offset = readInt(4)
    val attributes = bufferFrom(4).readAttributes(table)
    val name = table[readInt(4 + offset)]
    val types = bufferFrom(8 + offset).readListOfTypes(table)
    return FunctionInfoImpl(attributes, name, types)
}

fun Map<String, FunctionInfo>.writeTo(buffer: BufferWriter, table: BinaryTable) {
    buffer.add(size)
    forEach { (name, function) ->
        buffer.add(table.add(name))
        function.writeTo(buffer, table)
    }
}

fun BufferView.readFunctionList(table: BinaryTableView): Pair<Int, FunctionInfo> {
    val position = readInt(0)
    return position to readFunctionInfo(table)
}

fun BufferView.readFunctionInfoTable(table: BinaryTableView): Map<String, FunctionInfo> {
    val paramsSize = readInt(0)
    val types = mapBuilder<String, FunctionInfo>()
    var position = 4
    repeat(paramsSize) {
        val typeBuffer = bufferFrom(position)
        val key = table[typeBuffer.readInt(0)]
        val (newPosition, function) = typeBuffer.bufferFrom(4).readFunctionList(table)
        types.put(
            key,
            function
        )
        position += newPosition + 4
    }
    return types.build()
}
