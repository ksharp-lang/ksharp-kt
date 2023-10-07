package org.ksharp.module.bytecode

import org.ksharp.common.add
import org.ksharp.common.io.*
import org.ksharp.common.listBuilder
import org.ksharp.module.Impl
import org.ksharp.module.ModuleInfo
import org.ksharp.module.prelude.kernelTypeSystem
import org.ksharp.typesystem.serializer.readTypeSystem
import org.ksharp.typesystem.serializer.writeTo
import java.io.OutputStream

private fun List<String>.writeTo(buffer: BufferWriter, table: BinaryTable) {
    buffer.add(size)
    forEach {
        buffer.add(table.add(it))
    }
}

fun Set<Impl>.writeTo(buffer: BufferWriter, table: BinaryTable) {
    buffer.add(size)
    forEach { impl ->
        buffer.add(table.add(impl.trait))
        buffer.add(table.add(impl.type))
    }
}

fun BufferView.readImpls(table: BinaryTableView): Set<Impl> {
    val size = readInt(0)
    val result = mutableSetOf<Impl>()
    var position = 4
    repeat(size) {
        result.add(
            Impl(
                table[readInt(position)],
                table[readInt(position + 4)]
            )
        )
        position += 8
    }
    return result
}

private fun BufferView.readStringList(table: BinaryTableView): List<String> {
    val size = readInt(0)
    val result = listBuilder<String>()
    var position = 4
    repeat(size) {
        result.add(table[readInt(position)])
        position += 4
    }
    return result.build()
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
    val traitTable = newBufferWriter().apply {
        traits.writeTo(this, stringPool)
    }
    val implsTable = newBufferWriter().apply {
        impls.writeTo(this, stringPool)
    }
    val header = newBufferWriter()
    header.add(stringPool.size) // 0
    header.add(dependencies.size) // 4
    header.add(typeSystem.size) // 8
    header.add(functionTable.size) // 12
    header.add(traitTable.size) // 16

    header.transferTo(output)
    stringPool.writeTo(output)
    dependencies.transferTo(output)
    typeSystem.transferTo(output)
    functionTable.transferTo(output)
    traitTable.transferTo(output)
    implsTable.transferTo(output)
}

fun BufferView.readModuleInfo(): ModuleInfo {
    val stringPoolSize = readInt(0)
    val dependenciesSize = readInt(4)
    val typeSystemSize = readInt(8)
    val functionsSize = readInt(12)
    val traitsSize = readInt(16)
    val offset = 20

    val stringPool = StringPoolView(bufferFrom(offset))
    val dependencies = bufferFrom(offset + stringPoolSize).readStringList(stringPool)
    val kernelTypeSystem = kernelTypeSystem.value
    val typeSystem = bufferFrom(offset + dependenciesSize + stringPoolSize).readTypeSystem(stringPool, kernelTypeSystem)
    val functions =
        bufferFrom(offset + dependenciesSize + stringPoolSize + typeSystemSize).readFunctionInfoTable(stringPool)
    val traits =
        bufferFrom(offset + dependenciesSize + stringPoolSize + typeSystemSize + functionsSize)
            .readTraitInfoTable(stringPool)
    val impls = bufferFrom(offset + dependenciesSize + stringPoolSize + typeSystemSize + functionsSize + traitsSize)
        .readImpls(stringPool)
    return ModuleInfo(dependencies, typeSystem, functions, traits, impls)
}
