package org.ksharp.typesystem.serializer

import org.ksharp.common.HandlePromise
import org.ksharp.common.io.*
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo
import org.ksharp.typesystem.types.*


fun FunctionScope.writeTo(buffer: BufferWriter, table: BinaryTable) {
    buffer.add(type.ordinal)
    buffer.add(if (trait == null) -1 else table.add(trait))
}

fun BufferView.readFunctionScope(table: BinaryTableView): FunctionScope =
    FunctionScope(
        FunctionScopeType.entries[readInt(0)],
        readInt(4).let {
            if (it == -1) null else table[it]
        }
    )

class FunctionSerializer : SerializerWriter<FullFunctionType>, TypeSerializerReader<FullFunctionType> {
    override fun write(input: FullFunctionType, buffer: BufferWriter, table: BinaryTable) {
        input.scope.writeTo(buffer, table)
        input.attributes.writeTo(buffer, table)
        input.arguments.writeTo(buffer, table)
    }

    override fun read(handle: HandlePromise<TypeSystem>, buffer: BufferView, table: BinaryTableView): FullFunctionType =
        FullFunctionType(
            handle,
            buffer.bufferFrom(8).readAttributes(table),
            buffer.bufferFrom(8 + buffer.readInt(8)).readListOfTypes(handle, table),
            buffer.readFunctionScope(table)
        )
}

class PartialFunctionSerializer : SerializerWriter<PartialFunctionType>, TypeSerializerReader<PartialFunctionType> {
    override fun write(input: PartialFunctionType, buffer: BufferWriter, table: BinaryTable) {
        input.function.writeTo(buffer, table)
        input.arguments.writeTo(buffer, table)
    }

    override fun read(
        handle: HandlePromise<TypeSystem>,
        buffer: BufferView,
        table: BinaryTableView
    ): PartialFunctionType =
        PartialFunctionType(
            buffer.bufferFrom(buffer.readInt(0)).readListOfTypes(handle, table),
            buffer.readType<FunctionType>(handle, table)
        )
}
