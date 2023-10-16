package org.ksharp.typesystem.serializer

import org.ksharp.common.HandlePromise
import org.ksharp.common.io.*
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo
import org.ksharp.typesystem.types.FunctionType

class FunctionSerializer : SerializerWriter<FunctionType>, TypeSerializerReader<FunctionType> {
    override fun write(input: FunctionType, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        input.arguments.writeTo(buffer, table)
    }

    override fun read(handle: HandlePromise<TypeSystem>, buffer: BufferView, table: BinaryTableView): FunctionType =
        FunctionType(
            handle,
            buffer.readAttributes(table),
            buffer.bufferFrom(buffer.readInt(0)).readListOfTypes(handle, table)
        )

}
