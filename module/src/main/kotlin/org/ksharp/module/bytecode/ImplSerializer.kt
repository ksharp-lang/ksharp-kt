package org.ksharp.module.bytecode

import org.ksharp.common.HandlePromise
import org.ksharp.common.io.*
import org.ksharp.module.Impl
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.serializer.readType
import org.ksharp.typesystem.serializer.writeTo
import org.ksharp.typesystem.types.Type


fun Impl.writeTo(buffer: BufferWriter, table: BinaryTable) {
    newBufferWriter().apply {
        add(0) // 0
        add(table.add(module)) //4
        add(table.add(trait)) // 8
        type.writeTo(this, table) //12
        set(0, size)
        transferTo(buffer)
    }
}

fun BufferView.readImpl(handle: HandlePromise<TypeSystem>, table: BinaryTableView): Impl {
    val module = table[readInt(4)]
    val name = table[readInt(8)]
    val type = bufferFrom(12).readType<Type>(handle, table)
    return Impl(module, name, type)
}

fun Set<Impl>.writeTo(buffer: BufferWriter, table: BinaryTable) {
    buffer.add(size)
    forEach { impl ->
        impl.writeTo(buffer, table)
    }
}

fun BufferView.readImpls(handle: HandlePromise<TypeSystem>, table: BinaryTableView): Set<Impl> {
    val size = readInt(0)
    val result = mutableSetOf<Impl>()
    var position = 4
    repeat(size) {
        val implBuffer = bufferFrom(position)
        val impl = implBuffer.readImpl(handle, table)
        result.add(impl)
        position += implBuffer.readInt(0)
    }
    return result
}
