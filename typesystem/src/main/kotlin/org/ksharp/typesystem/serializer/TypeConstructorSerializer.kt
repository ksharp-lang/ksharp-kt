package org.ksharp.typesystem.serializer

import org.ksharp.common.io.*
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo
import org.ksharp.typesystem.types.TypeConstructor

class TypeConstructorSerializer : SerializerWriter<TypeConstructor>, SerializerReader<TypeConstructor> {
    override fun write(input: TypeConstructor, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        buffer.add(table.add(input.name))
        buffer.add(table.add(input.alias))
    }

    override fun read(buffer: BufferView, table: BinaryTableView): TypeConstructor =
        buffer.readInt(0).let { offset ->
            TypeConstructor(
                buffer.readAttributes(table),
                table[buffer.readInt(offset)],
                table[buffer.readInt(4 + offset)],
            )
        }

}
