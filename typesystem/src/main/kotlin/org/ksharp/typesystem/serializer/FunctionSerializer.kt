package org.ksharp.typesystem.serializer

import org.ksharp.common.io.*
import org.ksharp.typesystem.types.FunctionType

class FunctionSerializer : SerializerWriter<FunctionType>, SerializerReader<FunctionType> {
    override fun write(input: FunctionType, buffer: BufferWriter, table: BinaryTable) {
        buffer.writeTypeVisibility(input)
        input.arguments.writeTo(buffer, table)
    }

    override fun read(buffer: BufferView, table: BinaryTableView): FunctionType =
        FunctionType(buffer.readTypeVisibility(0), buffer.bufferFrom(4).readListOfTypes(table))

}