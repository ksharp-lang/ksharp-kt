package org.ksharp.module.bytecode

import org.ksharp.common.io.*
import org.ksharp.common.mapBuilder
import org.ksharp.common.put
import org.ksharp.module.TraitInfo
import org.ksharp.module.TraitInfoImpl
import org.ksharp.typesystem.serializer.readMapOfTypes
import org.ksharp.typesystem.serializer.writeTo

fun TraitInfo.writeTo(buffer: BufferWriter, table: BinaryTable) {
    val functionTable = newBufferWriter().apply {
        implementations.writeTo(this, table)
    }
    val definitionsTable = newBufferWriter().apply {
        definitions.writeTo(this, table)
    }
    newBufferWriter().apply {
        add(0) // 0
        add(table.add(name))
        definitionsTable.transferTo(this)
        functionTable.transferTo(this)
        set(0, size)
        transferTo(buffer)
    }
}

fun BufferView.readTraitInfo(table: BinaryTableView): TraitInfo {
    val name = table[readInt(4)]
    val definitions = bufferFrom(8).readMapOfTypes(table)
    val functions = bufferFrom(8 + readInt(8)).readFunctionInfoTable(table)
    return TraitInfoImpl(name, definitions, functions)
}

fun Map<String, TraitInfo>.writeTo(buffer: BufferWriter, table: BinaryTable) {
    buffer.add(size)
    forEach { (name, trait) ->
        buffer.add(table.add(name))
        trait.writeTo(buffer, table)
    }
}


fun BufferView.readTraitInfoAndPosition(table: BinaryTableView): Pair<Int, TraitInfo> {
    val position = readInt(0)
    return position to readTraitInfo(table)
}

fun BufferView.readTraitInfoTable(table: BinaryTableView): Map<String, TraitInfo> {
    val paramsSize = readInt(0)
    val types = mapBuilder<String, TraitInfo>()
    var position = 4
    repeat(paramsSize) {
        val typeBuffer = bufferFrom(position)
        val key = table[typeBuffer.readInt(0)]
        val (newPosition, function) = typeBuffer.bufferFrom(4).readTraitInfoAndPosition(table)
        types.put(
            key,
            function
        )
        position += newPosition + 4
    }
    return types.build()
}
