package org.ksharp.module.bytecode

import org.ksharp.common.HandlePromise
import org.ksharp.common.handlePromise
import org.ksharp.common.io.*
import org.ksharp.module.Impl
import org.ksharp.module.ModuleInfo
import org.ksharp.module.prelude.kernelTypeSystem
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.readMapOfStrings
import org.ksharp.typesystem.attributes.writeTo
import org.ksharp.typesystem.serializer.readType
import org.ksharp.typesystem.serializer.readTypeSystem
import org.ksharp.typesystem.serializer.writeTo
import java.io.OutputStream

fun Set<Impl>.writeTo(buffer: BufferWriter, table: BinaryTable) {
    buffer.add(size)
    forEach { impl ->
        buffer.add(table.add(impl.trait))
        impl.type.writeTo(buffer, table)
    }
}

fun BufferView.readImpls(handle: HandlePromise<TypeSystem>, table: BinaryTableView): Set<Impl> {
    val size = readInt(0)
    val result = mutableSetOf<Impl>()
    var position = 4
    repeat(size) {
        val typeSize = readInt(position + 4)
        result.add(
            Impl(
                table[readInt(position)],
                bufferFrom(position + 4).readType(handle, table)
            )
        )
        position += 4 + typeSize
    }
    return result
}

fun ModuleInfo.writeTo(output: OutputStream) {
    val stringPool = StringPoolBuilder()
    val dependencies = newBufferWriter().apply {
        dependencies.writeTo(this, stringPool)
    }
    val typeSystem = newBufferWriter().apply {
        typeSystem.writeTo(this, stringPool)
    }
    val functionTable = newBufferWriter().apply {
        functions.writeTo(this, stringPool)
    }
    val implsTable = newBufferWriter().apply {
        impls.writeTo(this, stringPool)
    }
    val header = newBufferWriter()
    header.add(stringPool.size) // 0
    header.add(dependencies.size) // 4
    header.add(typeSystem.size) // 8
    header.add(functionTable.size) // 12

    header.transferTo(output)
    stringPool.writeTo(output)
    dependencies.transferTo(output)
    typeSystem.transferTo(output)
    functionTable.transferTo(output)
    implsTable.transferTo(output)
}

fun BufferView.readModuleInfo(handle: HandlePromise<TypeSystem> = handlePromise()): ModuleInfo {
    val stringPoolSize = readInt(0)
    val dependenciesSize = readInt(4)
    val typeSystemSize = readInt(8)
    val functionsSize = readInt(12)
    val offset = 16

    val stringPool = StringPoolView(bufferFrom(offset))
    val dependencies = bufferFrom(offset + stringPoolSize).readMapOfStrings(stringPool)
    val kernelTypeSystem = kernelTypeSystem.value
    val typeSystem =
        bufferFrom(offset + dependenciesSize + stringPoolSize).readTypeSystem(stringPool, kernelTypeSystem, handle)
    val functions =
        bufferFrom(offset + dependenciesSize + stringPoolSize + typeSystemSize).readFunctionInfoTable(
            typeSystem.handle,
            stringPool
        )
    val impls = bufferFrom(offset + dependenciesSize + stringPoolSize + typeSystemSize + functionsSize)
        .readImpls(typeSystem.handle, stringPool)
    return ModuleInfo(dependencies, typeSystem, functions, impls)
}
