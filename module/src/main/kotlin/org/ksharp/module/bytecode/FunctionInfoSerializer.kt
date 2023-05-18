package org.ksharp.module.bytecode

import org.ksharp.common.add
import org.ksharp.common.io.*
import org.ksharp.common.listBuilder
import org.ksharp.common.mapBuilder
import org.ksharp.common.put
import org.ksharp.module.FunctionInfo
import org.ksharp.module.FunctionVisibility
import org.ksharp.typesystem.serializer.readListOfTypes
import org.ksharp.typesystem.serializer.writeTo

fun FunctionInfo.writeTo(buffer: BufferWriter, table: BinaryTable) {
    newBufferWriter().apply {
        add(0)
        add(visibility.ordinal)
        if (dependency == null) {
            add(-1)
        } else add(table.add(dependency))
        add(table.add(name))
        types.writeTo(this, table)
        set(0, size)
        transferTo(buffer)
    }
}

fun BufferView.readFunctionInfo(table: BinaryTableView): FunctionInfo {
    val visibility = readInt(4).let { FunctionVisibility.values()[it] }
    val dependency = readInt(8).let {
        if (it == -1) null
        else table[it]
    }
    val name = table[readInt(12)]
    val types = bufferFrom(16).readListOfTypes(table)
    return FunctionInfo(visibility, dependency, name, types)
}

fun List<FunctionInfo>.writeTo(buffer: BufferWriter, table: BinaryTable) {
    buffer.add(size)
    forEach {
        it.writeTo(buffer, table)
    }
}

fun Map<String, List<FunctionInfo>>.writeTo(buffer: BufferWriter, table: BinaryTable) {
    buffer.add(size)
    forEach { (name, function) ->
        buffer.add(table.add(name))
        function.writeTo(buffer, table)
    }
}

fun BufferView.readFunctionList(table: BinaryTableView): Pair<Int, List<FunctionInfo>> {
    val size = readInt(0)
    val result = listBuilder<FunctionInfo>()
    var position = 4
    repeat(size) {
        val functionBuffer = bufferFrom(position)
        position += functionBuffer.readInt(0)
        result.add(functionBuffer.readFunctionInfo(table))
    }
    return position to result.build()
}

fun BufferView.readFunctionInfoTable(table: BinaryTableView): Map<String, List<FunctionInfo>> {
    val paramsSize = readInt(0)
    val types = mapBuilder<String, List<FunctionInfo>>()
    var position = 4
    repeat(paramsSize) {
        val typeBuffer = bufferFrom(position)
        val key = table[typeBuffer.readInt(0)]
        val (newPosition, functions) = typeBuffer.bufferFrom(4).readFunctionList(table)
        types.put(
            key,
            functions
        )
        position += newPosition + 4
    }
    return types.build()
}