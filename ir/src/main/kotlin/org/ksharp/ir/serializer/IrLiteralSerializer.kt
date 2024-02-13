package org.ksharp.ir.serializer

import org.ksharp.common.io.BinaryTable
import org.ksharp.common.io.BinaryTableView
import org.ksharp.common.io.BufferView
import org.ksharp.common.io.BufferWriter
import org.ksharp.ir.*

class IrIntegerSerializer : IrNodeSerializer<IrInteger> {
    override fun write(input: IrInteger, buffer: BufferWriter, table: BinaryTable) {
        buffer.add(input.value)
        input.location.writeTo(buffer)
    }

    override fun read(
        lookup: FunctionLookup,
        loader: LoadIrModuleFn,
        buffer: BufferView,
        table: BinaryTableView
    ): IrInteger {
        return IrInteger(
            buffer.readLong(0),
            buffer.bufferFrom(8).readLocation()
        )
    }

}

class IrDecimalSerializer : IrNodeSerializer<IrDecimal> {
    override fun write(input: IrDecimal, buffer: BufferWriter, table: BinaryTable) {
        buffer.add(input.value)
        input.location.writeTo(buffer)
    }

    override fun read(
        lookup: FunctionLookup,
        loader: LoadIrModuleFn,
        buffer: BufferView,
        table: BinaryTableView
    ): IrDecimal {
        return IrDecimal(
            buffer.readDouble(0),
            buffer.bufferFrom(8).readLocation()
        )
    }

}

class IrCharacterSerializer : IrNodeSerializer<IrCharacter> {
    override fun write(input: IrCharacter, buffer: BufferWriter, table: BinaryTable) {
        buffer.add(input.value.code)
        input.location.writeTo(buffer)
    }

    override fun read(
        lookup: FunctionLookup,
        loader: LoadIrModuleFn,
        buffer: BufferView,
        table: BinaryTableView
    ): IrCharacter {
        return IrCharacter(
            buffer.readInt(0).toChar(),
            buffer.bufferFrom(4).readLocation()
        )
    }

}

class IrStringSerializer : IrNodeSerializer<IrString> {
    override fun write(input: IrString, buffer: BufferWriter, table: BinaryTable) {
        buffer.add(table.add(input.value))
        input.location.writeTo(buffer)
    }

    override fun read(
        lookup: FunctionLookup,
        loader: LoadIrModuleFn,
        buffer: BufferView,
        table: BinaryTableView
    ): IrString {
        return IrString(
            table[buffer.readInt(0)],
            buffer.bufferFrom(4).readLocation()
        )
    }

}

class IrBoolSerializer : IrNodeSerializer<IrBool> {
    override fun write(input: IrBool, buffer: BufferWriter, table: BinaryTable) {
        buffer.add(if (input.value) 1 else 0)
        input.location.writeTo(buffer)
    }

    override fun read(
        lookup: FunctionLookup,
        loader: LoadIrModuleFn,
        buffer: BufferView,
        table: BinaryTableView
    ): IrBool {
        return IrBool(
            buffer.readInt(0) == 1,
            buffer.bufferFrom(4).readLocation()
        )
    }

}
