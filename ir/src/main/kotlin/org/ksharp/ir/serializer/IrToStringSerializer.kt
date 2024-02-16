package org.ksharp.ir.serializer

import org.ksharp.common.cast
import org.ksharp.common.io.BinaryTable
import org.ksharp.common.io.BinaryTableView
import org.ksharp.common.io.BufferView
import org.ksharp.common.io.BufferWriter
import org.ksharp.ir.FunctionLookup
import org.ksharp.ir.IrToString
import org.ksharp.ir.LoadIrModuleFn

class IrToStringSerializer : IrNodeSerializer<IrToString> {
    override fun write(input: IrToString, buffer: BufferWriter, table: BinaryTable) {
        input.expr.serialize(buffer, table)
        input.location.writeTo(buffer)
    }

    override fun read(
        lookup: FunctionLookup,
        loader: LoadIrModuleFn,
        buffer: BufferView,
        table: BinaryTableView
    ): IrToString {
        val expr = buffer.readIrNode(lookup, loader, table)
        val offset = buffer.readInt(0)
        val location = buffer.bufferFrom(offset).readLocation()
        return IrToString(expr.cast(), location)
    }
}
