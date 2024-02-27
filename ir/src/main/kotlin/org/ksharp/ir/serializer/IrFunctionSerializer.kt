package org.ksharp.ir.serializer

import org.ksharp.common.add
import org.ksharp.common.cast
import org.ksharp.common.io.BinaryTable
import org.ksharp.common.io.BinaryTableView
import org.ksharp.common.io.BufferView
import org.ksharp.common.io.BufferWriter
import org.ksharp.common.listBuilder
import org.ksharp.ir.FunctionLookup
import org.ksharp.ir.IrFunction
import org.ksharp.ir.IrLambda
import org.ksharp.ir.LoadIrModuleFn
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo

private fun List<String>.writeTo(buffer: BufferWriter, table: BinaryTable) {
    buffer.add(size)
    forEach { value ->
        buffer.add(table.add(value))
    }
}

private fun BufferView.readListOfStrings(table: BinaryTableView): List<String> {
    val paramsSize = readInt(0)
    val result = listBuilder<String>()
    var position = 4
    repeat(paramsSize) {
        val value = table[readInt(position)]
        position += 4
        result.add(value)
    }
    return result.build()
}

class IrFunctionSerializer : IrNodeSerializer<IrFunction> {
    override fun write(input: IrFunction, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        buffer.add(table.add(input.name))
        input.arguments.writeTo(buffer, table)
        buffer.add(input.frameSlots)
        input.expr.serialize(buffer, table)
        input.location.writeTo(buffer)
    }

    override fun read(
        lookup: FunctionLookup,
        loader: LoadIrModuleFn,
        buffer: BufferView,
        table: BinaryTableView
    ): IrFunction {
        var offset = buffer.readInt(0)
        val attributes = buffer.readAttributes(table)
        val name = table[buffer.readInt(offset)]
        offset += 4
        val arguments = buffer.bufferFrom(offset).readListOfStrings(table)
        offset += 4 + arguments.size * 4
        val frameSlot = buffer.readInt(offset)
        offset += 4
        val expr = buffer.bufferFrom(offset).readIrNode(lookup, loader, table)
        offset += buffer.readInt(offset)
        val location = buffer.bufferFrom(offset).readLocation()
        return IrFunction(
            attributes,
            name,
            arguments,
            frameSlot,
            expr.cast(),
            location
        )
    }
}

class IrLambdaSerializer : IrNodeSerializer<IrLambda> {
    override fun write(input: IrLambda, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        input.arguments.writeTo(buffer, table)
        buffer.add(input.frameSlots)
        input.expr.serialize(buffer, table)
        input.location.writeTo(buffer)
    }

    override fun read(
        lookup: FunctionLookup,
        loader: LoadIrModuleFn,
        buffer: BufferView,
        table: BinaryTableView
    ): IrLambda {
        var offset = buffer.readInt(0)
        val attributes = buffer.readAttributes(table)
        val arguments = buffer.bufferFrom(offset).readListOfStrings(table)
        offset += 4 + arguments.size * 4
        val frameSlot = buffer.readInt(offset)
        offset += 4
        val expr = buffer.bufferFrom(offset).readIrNode(lookup, loader, table)
        offset += buffer.readInt(offset)
        val location = buffer.bufferFrom(offset).readLocation()
        return IrLambda(
            attributes,
            arguments,
            frameSlot,
            expr.cast(),
            location
        )
    }
}
