package org.ksharp.typesystem.serializer

import org.ksharp.common.io.*
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo
import org.ksharp.typesystem.types.Alias
import org.ksharp.typesystem.types.TypeAlias

class AliasSerializer : SerializerWriter<Alias>, SerializerReader<Alias> {
    override fun write(input: Alias, buffer: BufferWriter, table: BinaryTable) {
        buffer.add(table.add(input.name))
    }

    override fun read(buffer: BufferView, table: BinaryTableView): Alias =
        Alias(
            table[buffer.readInt(0)],
        )

}

class TypeAliasSerializer : SerializerWriter<TypeAlias>, SerializerReader<TypeAlias> {
    override fun write(input: TypeAlias, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        buffer.add(table.add(input.name))
    }

    override fun read(buffer: BufferView, table: BinaryTableView): TypeAlias =
        TypeAlias(
            buffer.readAttributes(table),
            table[buffer.readInt(buffer.readInt(0))],
        )

}
