package org.ksharp.typesystem.serializer

import org.ksharp.common.HandlePromise
import org.ksharp.common.io.*
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo
import org.ksharp.typesystem.types.FullFunctionType
import org.ksharp.typesystem.types.FunctionType
import org.ksharp.typesystem.types.PartialFunctionType

class FunctionSerializer : SerializerWriter<FullFunctionType>, TypeSerializerReader<FullFunctionType> {
    override fun write(input: FullFunctionType, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        input.arguments.writeTo(buffer, table)
    }

    override fun read(handle: HandlePromise<TypeSystem>, buffer: BufferView, table: BinaryTableView): FullFunctionType =
        FullFunctionType(
            handle,
            buffer.readAttributes(table),
            buffer.bufferFrom(buffer.readInt(0)).readListOfTypes(handle, table)
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
