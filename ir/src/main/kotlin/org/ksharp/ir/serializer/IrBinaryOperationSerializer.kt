package org.ksharp.ir.serializer

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.common.io.BinaryTable
import org.ksharp.common.io.BinaryTableView
import org.ksharp.common.io.BufferView
import org.ksharp.common.io.BufferWriter
import org.ksharp.ir.FunctionLookup
import org.ksharp.ir.IrBinaryOperation
import org.ksharp.ir.IrExpression
import org.ksharp.ir.LoadIrModuleFn
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo

typealias IrBinaryNodeFactory = (
    attribute: Set<Attribute>,
    left: IrExpression,
    right: IrExpression,
    location: Location
) -> IrExpression

internal fun BufferView.readIrBinaryNode(
    lookup: FunctionLookup,
    loader: LoadIrModuleFn,
    table: BinaryTableView,
    factory: IrBinaryNodeFactory
): IrExpression {
    var offset = this.readInt(0)
    val attributes = this.readAttributes(table)

    val left = this.bufferFrom(offset).readIrNode(lookup, loader, table)
    offset += this.readInt(offset)

    val right = this.bufferFrom(offset).readIrNode(lookup, loader, table)
    offset += this.readInt(offset)

    val location = this.bufferFrom(offset).readLocation()
    return factory(attributes, left.cast(), right.cast(), location)
}

class IrBinaryOperationSerializer(private val factory: IrBinaryNodeFactory) : IrNodeSerializer<IrBinaryOperation> {

    override fun write(input: IrBinaryOperation, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        input.left.serialize(buffer, table)
        input.right.serialize(buffer, table)
        input.location.writeTo(buffer)
    }

    override fun read(
        lookup: FunctionLookup,
        loader: LoadIrModuleFn,
        buffer: BufferView,
        table: BinaryTableView
    ): IrBinaryOperation {
        return buffer.readIrBinaryNode(lookup, loader, table, factory).cast()
    }
}
