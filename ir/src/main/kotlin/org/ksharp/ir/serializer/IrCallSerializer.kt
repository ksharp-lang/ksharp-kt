package org.ksharp.ir.serializer

import org.ksharp.common.cast
import org.ksharp.common.io.BinaryTable
import org.ksharp.common.io.BinaryTableView
import org.ksharp.common.io.BufferView
import org.ksharp.common.io.BufferWriter
import org.ksharp.ir.CallScope
import org.ksharp.ir.FunctionLookup
import org.ksharp.ir.IrCall
import org.ksharp.ir.IrNativeCall
import org.ksharp.module.prelude.preludeModule
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo
import org.ksharp.typesystem.serializer.readType
import org.ksharp.typesystem.serializer.writeTo
import org.ksharp.typesystem.types.Type


private fun CallScope.writeTo(buffer: BufferWriter, table: BinaryTable) {
    buffer.add(table.add(this.callName))
    if (traitType == null) {
        buffer.add(0)
    } else {
        buffer.add(1)
        traitType.writeTo(buffer, table)
    }
}

private fun BufferView.readCallScope(table: BinaryTableView): Pair<Int, CallScope> {
    val callName = table[readInt(0)]
    val hasTrait = readInt(4) == 1
    val (position, trait) = if (hasTrait) {
        bufferFrom(8).let { typeBuffer ->
            (8 + typeBuffer.readInt(0)) to typeBuffer.readType<Type>(preludeModule.typeSystem.handle, table)
        }
    } else 8 to null
    return position to CallScope(callName, trait)
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

    override fun read(lookup: FunctionLookup, buffer: BufferView, table: BinaryTableView): IrCall {
        val attributes = buffer.readAttributes(table)
        var offset = buffer.readInt(0)
        val module = buffer.readInt(offset).let { if (it == -1) null else table[it] }
        offset += 4
        val (callScopePosition, callScope) = buffer.bufferFrom(offset).readCallScope(table)
        offset += callScopePosition
        val location = buffer.bufferFrom(offset).readLocation()
        offset += 16
        val arguments = buffer.bufferFrom(offset).readListOfNodes(lookup, table).second
        return IrCall(
            attributes,
            module,
            callScope,
            arguments.cast(),
            location
        )
    }
}

class IrNativeCallSerializer : IrNodeSerializer<IrNativeCall> {
    override fun write(input: IrNativeCall, buffer: BufferWriter, table: BinaryTable) {
        input.argAttributes.writeTo(buffer, table)
        buffer.add(table.add(input.functionClass))
        input.location.writeTo(buffer)
        input.arguments.writeTo(buffer, table)
    }

    override fun read(lookup: FunctionLookup, buffer: BufferView, table: BinaryTableView): IrNativeCall {
        val argAttributes = buffer.readAttributes(table)
        var offset = buffer.readInt(0)
        val functionClass = table[buffer.readInt(offset)]
        offset += 4
        val location = buffer.bufferFrom(offset).readLocation()
        offset += 16
        val arguments = buffer.bufferFrom(offset).readListOfNodes(lookup, table).second
        return IrNativeCall(
            argAttributes,
            functionClass,
            arguments.cast(),
            location
        )
    }
}
