package org.ksharp.typesystem.serializer

import org.ksharp.common.io.*
import org.ksharp.typesystem.types.TupleType

class TupleSerializer : SerializerWriter<TupleType>, SerializerReader<TupleType> {

    override fun read(buffer: BufferView, table: BinaryTableView): TupleType =
        TupleType(buffer.readListOfTypes(table))

    override fun write(input: TupleType, buffer: BufferWriter, table: BinaryTable) {
        input.elements.writeTo(buffer, table)
    }
}