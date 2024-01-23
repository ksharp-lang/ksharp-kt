package org.ksharp.ir.serializer

import org.ksharp.common.cast
import org.ksharp.common.io.BinaryTable
import org.ksharp.common.io.BinaryTableView
import org.ksharp.common.io.BufferView
import org.ksharp.common.io.BufferWriter
import org.ksharp.ir.IrLet
import org.ksharp.ir.IrSetVar
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo

class IrLetSerializer : IrNodeSerializer<IrLet> {
    override fun write(input: IrLet, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        input.location.writeTo(buffer)
        input.expressions.writeTo(buffer, table)
    }

    override fun read(buffer: BufferView, table: BinaryTableView): IrLet {
        val attributes = buffer.readAttributes(table)
        var offset = buffer.readInt(0)
        val location = buffer.bufferFrom(offset).readLocation()
        offset += 16
        val expressions = buffer.bufferFrom(offset).readListOfNodes(table).second
        return IrLet(
            attributes,
            expressions.cast(),
            location
        )
    }
}

class IrSetVarSerializer : IrNodeSerializer<IrSetVar> {
    override fun write(input: IrSetVar, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        buffer.add(input.index)
        input.value.serialize(buffer, table)
        input.location.writeTo(buffer)
    }

    override fun read(buffer: BufferView, table: BinaryTableView): IrSetVar {
        val attributes = buffer.readAttributes(table)
        var offset = buffer.readInt(0)
        val index = buffer.readInt(offset)
        offset += 4
        val value = buffer.bufferFrom(offset).readIrNode(table)
        offset += buffer.readInt(offset)
        val location = buffer.bufferFrom(offset).readLocation()
        return IrSetVar(
            attributes,
            index,
            value.cast(),
            location
        )
    }

}
