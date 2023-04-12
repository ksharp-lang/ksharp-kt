package org.ksharp.module.bytecode

import org.ksharp.common.io.*
import org.ksharp.common.mapBuilder
import org.ksharp.common.put
import org.ksharp.module.FunctionInfo
import org.ksharp.typesystem.serializer.readListOfTypes
import org.ksharp.typesystem.serializer.writeTo

fun FunctionInfo.writeTo(buffer: BufferWriter, table: BinaryTable) {
    newBufferWriter().apply {
        add(0)
        add(table.add(name))
        types.writeTo(this, table)
        set(0, size)
        transferTo(buffer)
    }
}

fun BufferView.readFunctionInfo(table: BinaryTableView): FunctionInfo {
    val name = table[readInt(4)]
    val types = bufferFrom(8).readListOfTypes(table)
    return FunctionInfo(name, types)
}

fun Map<String, FunctionInfo>.writeTo(buffer: BufferWriter, table: BinaryTable) {
    buffer.add(size)
    forEach { (name, function) ->
        buffer.add(table.add(name))
        function.writeTo(buffer, table)
    }
}

fun BufferView.readFunctionInfoTable(table: BinaryTableView): Map<String, FunctionInfo> {
    val paramsSize = readInt(0)
    val types = mapBuilder<String, FunctionInfo>()
    var position = 4
    repeat(paramsSize) {
        val typeBuffer = bufferFrom(position)
        val key = table[typeBuffer.readInt(0)]
        position += typeBuffer.readInt(4) + 4
        types.put(
            key,
            typeBuffer.bufferFrom(4).readFunctionInfo(table)
        )
    }
    return types.build()
}