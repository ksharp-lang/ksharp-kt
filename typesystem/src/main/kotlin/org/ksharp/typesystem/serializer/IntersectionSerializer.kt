package org.ksharp.typesystem.serializer

import org.ksharp.common.cast
import org.ksharp.common.io.*
import org.ksharp.typesystem.types.Alias
import org.ksharp.typesystem.types.IntersectionType

class IntersectionSerializer : SerializerWriter<IntersectionType>, SerializerReader<IntersectionType> {
    override fun write(input: IntersectionType, buffer: BufferWriter, table: BinaryTable) {
        input.params.writeTo(buffer, table)
    }

    override fun read(buffer: BufferView, table: BinaryTableView): IntersectionType = IntersectionType(
        buffer.readListOfTypes(table).cast<List<Alias>>()
    )
}