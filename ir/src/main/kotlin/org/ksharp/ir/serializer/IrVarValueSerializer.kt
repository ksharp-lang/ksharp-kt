package org.ksharp.ir.serializer

import org.ksharp.common.Location
import org.ksharp.common.io.BinaryTable
import org.ksharp.common.io.BinaryTableView
import org.ksharp.common.io.BufferView
import org.ksharp.common.io.BufferWriter
import org.ksharp.ir.FunctionLookup
import org.ksharp.ir.IrValueAccess
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo

typealias IrVarAccessFactory = (
    attribute: Set<Attribute>,
    index: Int,
    location: Location
) -> IrValueAccess

class IrVarValueSerializer(private val factory: IrVarAccessFactory) : IrNodeSerializer<IrValueAccess> {
    override fun write(input: IrValueAccess, buffer: BufferWriter, table: BinaryTable) {
        buffer.add(input.index)
        input.location.writeTo(buffer)
        input.attributes.writeTo(buffer, table)
    }

    override fun read(lookup: FunctionLookup, buffer: BufferView, table: BinaryTableView): IrValueAccess =
        factory(
            buffer.bufferFrom(20).readAttributes(table),
            buffer.readInt(0),
            buffer.bufferFrom(4).readLocation()
        )
}
