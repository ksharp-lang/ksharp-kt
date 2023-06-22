package org.ksharp.typesystem.serializer

import org.ksharp.common.io.*
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo
import org.ksharp.typesystem.types.Alias

class AliasSerializer : SerializerWriter<Alias>, SerializerReader<Alias> {
    override fun write(input: Alias, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        buffer.add(table.add(input.name))
    }

    override fun read(buffer: BufferView, table: BinaryTableView): Alias =
        Alias(
            buffer.readAttributes(table),
            table[buffer.readInt(buffer.readInt(0))]
        )

}
