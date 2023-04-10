package org.ksharp.typesystem.serializer

import org.ksharp.common.io.*
import org.ksharp.typesystem.types.Alias

class AliasSerializer : SerializerWriter<Alias>, SerializerReader<Alias> {
    override fun write(input: Alias, buffer: BufferWriter, table: BinaryTable) {
        buffer.add(table.add(input.name))
    }

    override fun read(buffer: BufferView, table: BinaryTableView): Alias =
        Alias(table[buffer.readInt(0)])

}