package org.ksharp.typesystem.serializer

import org.ksharp.common.io.*
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo
import org.ksharp.typesystem.types.TupleType

class TupleSerializer : SerializerWriter<TupleType>, SerializerReader<TupleType> {

    override fun read(buffer: BufferView, table: BinaryTableView): TupleType =
        TupleType(buffer.readAttributes(table), buffer.bufferFrom(buffer.readInt(0)).readListOfTypes(table))

    override fun write(input: TupleType, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        input.elements.writeTo(buffer, table)
    }
}
