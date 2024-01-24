package org.ksharp.ir.serializer

import org.ksharp.common.cast
import org.ksharp.common.io.BinaryTable
import org.ksharp.common.io.BinaryTableView
import org.ksharp.common.io.BufferView
import org.ksharp.common.io.BufferWriter
import org.ksharp.ir.FunctionLookup
import org.ksharp.ir.IrArithmeticCall

class IrArithmeticCallSerializer : IrNodeSerializer<IrArithmeticCall> {
    override fun write(input: IrArithmeticCall, buffer: BufferWriter, table: BinaryTable) {
        buffer.add(table.add(input.name))
        input.expr.serialize(buffer, table)
    }

    override fun read(
        lookup: FunctionLookup,
        buffer: BufferView,
        table: BinaryTableView
    ): IrArithmeticCall {
        val name = table[buffer.readInt(0)]
        val expr = buffer.bufferFrom(4).readIrNode(lookup, table)
        return IrArithmeticCall(name, expr.cast())
    }
}
