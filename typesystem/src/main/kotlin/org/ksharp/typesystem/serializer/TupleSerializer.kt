package org.ksharp.typesystem.serializer

import org.ksharp.common.HandlePromise
import org.ksharp.common.io.*
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo
import org.ksharp.typesystem.types.TupleType

class TupleSerializer : SerializerWriter<TupleType>, TypeSerializerReader<TupleType> {

    override fun read(handle: HandlePromise<TypeSystem>, buffer: BufferView, table: BinaryTableView): TupleType =
        TupleType(
            handle,
            buffer.readAttributes(table),
            buffer.bufferFrom(buffer.readInt(0)).readListOfTypes(handle, table)
        )

    override fun write(input: TupleType, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        input.elements.writeTo(buffer, table)
    }
}
