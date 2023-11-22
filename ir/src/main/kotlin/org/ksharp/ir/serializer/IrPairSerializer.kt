package org.ksharp.ir.serializer

import org.ksharp.common.cast
import org.ksharp.common.io.BinaryTable
import org.ksharp.common.io.BinaryTableView
import org.ksharp.common.io.BufferView
import org.ksharp.common.io.BufferWriter
import org.ksharp.ir.IrPair
import org.ksharp.typesystem.attributes.writeTo

class IrPairSerializer : IrNodeSerializer<IrPair> {
    override fun write(input: IrPair, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        input.first.serialize(buffer, table)
        input.second.serialize(buffer, table)
        input.location.writeTo(buffer)
    }

    override fun read(buffer: BufferView, table: BinaryTableView): IrPair {
        return buffer.readIrBinaryNode(table, ::IrPair).cast()
    }
}
