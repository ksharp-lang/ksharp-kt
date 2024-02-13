package org.ksharp.ir.serializer

import org.ksharp.common.cast
import org.ksharp.common.io.BinaryTable
import org.ksharp.common.io.BinaryTableView
import org.ksharp.common.io.BufferView
import org.ksharp.common.io.BufferWriter
import org.ksharp.ir.CastType
import org.ksharp.ir.FunctionLookup
import org.ksharp.ir.IrNumCast
import org.ksharp.ir.LoadIrModuleFn

class IrNumCastSerializer : IrNodeSerializer<IrNumCast> {
    override fun write(input: IrNumCast, buffer: BufferWriter, table: BinaryTable) {
        input.expr.serialize(buffer, table)
        buffer.add(input.type.ordinal)
        input.location.writeTo(buffer)
    }

    override fun read(
        lookup: FunctionLookup,
        loader: LoadIrModuleFn,
        buffer: BufferView,
        table: BinaryTableView
    ): IrNumCast {
        val expr = buffer.readIrNode(lookup, loader, table)
        val offset = buffer.readInt(0)
        val type = CastType.entries[buffer.readInt(offset)]
        val location = buffer.bufferFrom(offset + 4).readLocation()
        return IrNumCast(expr.cast(), type, location)
    }
}
