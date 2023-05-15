package org.ksharp.typesystem.serializer

import org.ksharp.common.io.*
import org.ksharp.typesystem.types.TypeConstructor

class TypeConstructorSerializer : SerializerWriter<TypeConstructor>, SerializerReader<TypeConstructor> {
    override fun write(input: TypeConstructor, buffer: BufferWriter, table: BinaryTable) {
        buffer.add(table.add(input.name))
        buffer.add(table.add(input.alias))
    }

    override fun read(buffer: BufferView, table: BinaryTableView): TypeConstructor =
        TypeConstructor(
            table[buffer.readInt(0)],
            table[buffer.readInt(4)],
        )

}