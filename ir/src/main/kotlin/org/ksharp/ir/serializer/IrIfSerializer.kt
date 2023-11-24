package org.ksharp.ir.serializer

import org.ksharp.common.cast
import org.ksharp.common.io.BinaryTable
import org.ksharp.common.io.BinaryTableView
import org.ksharp.common.io.BufferView
import org.ksharp.common.io.BufferWriter
import org.ksharp.ir.IrExpression
import org.ksharp.ir.IrIf
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo

class IrIfSerializer : IrNodeSerializer<IrIf> {
    override fun write(input: IrIf, buffer: BufferWriter, table: BinaryTable) {
        input.location.writeTo(buffer)
        input.attributes.writeTo(buffer, table)
        listOf(input.condition, input.thenExpr, input.elseExpr).writeTo(buffer, table)
    }

    override fun read(buffer: BufferView, table: BinaryTableView): IrIf {
        val location = buffer.readLocation()
        val attributes = buffer.bufferFrom(16).readAttributes(table)
        val exprs = buffer.bufferFrom(16 + buffer.readInt(16))
            .readListOfNodes(table)
            .cast<List<IrExpression>>()
        return IrIf(attributes, exprs[0], exprs[1], exprs[2], location)
    }
}
