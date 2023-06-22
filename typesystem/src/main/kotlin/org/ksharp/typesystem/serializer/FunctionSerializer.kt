package org.ksharp.typesystem.serializer

import org.ksharp.common.io.*
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo
import org.ksharp.typesystem.types.FunctionType

class FunctionSerializer : SerializerWriter<FunctionType>, SerializerReader<FunctionType> {
    override fun write(input: FunctionType, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        input.arguments.writeTo(buffer, table)
    }

    override fun read(buffer: BufferView, table: BinaryTableView): FunctionType =
        FunctionType(buffer.readAttributes(table), buffer.bufferFrom(buffer.readInt(0)).readListOfTypes(table))

}
