package org.ksharp.typesystem.serializer

import org.ksharp.common.io.*
import org.ksharp.typesystem.types.FunctionType

class FunctionSerializer : SerializerWriter<FunctionType>, SerializerReader<FunctionType> {
    override fun write(input: FunctionType, buffer: BufferWriter, table: BinaryTable) {
        input.arguments.writeTo(buffer, table)
    }

    override fun read(buffer: BufferView, table: BinaryTableView): FunctionType =
        FunctionType(buffer.readListOfTypes(table))

}