package org.ksharp.ir.serializer

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.common.io.BinaryTable
import org.ksharp.common.io.BinaryTableView
import org.ksharp.common.io.BufferView
import org.ksharp.common.io.BufferWriter
import org.ksharp.ir.IrBinaryOperation
import org.ksharp.ir.IrExpression
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo

typealias IrBinaryOperationFactory = (
    attribute: Set<Attribute>,
    left: IrExpression,
    right: IrExpression,
    location: Location
) -> IrBinaryOperation

class IrBinaryOperationSerializer(val factory: IrBinaryOperationFactory) : IrNodeSerializer<IrBinaryOperation> {

    override fun write(input: IrBinaryOperation, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        input.left.serialize(buffer, table)
        input.right.serialize(buffer, table)
        input.location.writeTo(buffer)
    }

    override fun read(buffer: BufferView, table: BinaryTableView): IrBinaryOperation {
        var offset = buffer.readInt(0)
        val attributes = buffer.readAttributes(table)

        val left = buffer.bufferFrom(offset).readIrNode(table)
        offset += buffer.readInt(offset)

        val right = buffer.bufferFrom(offset).readIrNode(table)
        offset += buffer.readInt(offset)

        val location = buffer.bufferFrom(offset).readLocation()
        return factory(attributes, left.cast(), right.cast(), location)
    }
}
