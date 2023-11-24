package org.ksharp.ir.serializer

import org.ksharp.common.cast
import org.ksharp.common.io.BinaryTable
import org.ksharp.common.io.BinaryTableView
import org.ksharp.common.io.BufferView
import org.ksharp.common.io.BufferWriter
import org.ksharp.ir.CallScope
import org.ksharp.ir.IrCall
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo


private fun CallScope.writeTo(buffer: BufferWriter, table: BinaryTable) {
    buffer.add(table.add(this.callName))
    this.traitScopeName
        .let { if (it == null) -1 else table.add(it) }
        .let { buffer.add(it) }
    buffer.add(if (isFirstArgTrait) 1 else 0)
}

private fun BufferView.readCallScope(table: BinaryTableView): CallScope {
    val callName = table[readInt(0)]
    val traitScopeName = readInt(4).let { if (it == -1) null else table[it] }
    val isFirstArgTrait = readInt(8) == 1
    return CallScope(callName, traitScopeName, isFirstArgTrait)
}

class IrCallSerializer : IrNodeSerializer<IrCall> {
    override fun write(input: IrCall, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        input.module
            .let { if (it == null) -1 else table.add(it) }
            .let { buffer.add(it) }
        input.scope.writeTo(buffer, table)
        input.location.writeTo(buffer)
        input.arguments.writeTo(buffer, table)
    }

    override fun read(buffer: BufferView, table: BinaryTableView): IrCall {
        val attributes = buffer.readAttributes(table)
        var offset = buffer.readInt(0)
        val module = buffer.readInt(offset).let { if (it == -1) null else table[it] }
        offset += 4
        val callScope = buffer.bufferFrom(offset).readCallScope(table)
        offset += 12
        val location = buffer.bufferFrom(offset).readLocation()
        offset += 16
        val arguments = buffer.bufferFrom(offset).readListOfNodes(table)
        return IrCall(
            attributes,
            module,
            callScope,
            arguments.cast(),
            location
        )
    }
}