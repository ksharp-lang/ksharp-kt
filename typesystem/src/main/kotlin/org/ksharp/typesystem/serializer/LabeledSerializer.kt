package org.ksharp.typesystem.serializer

import org.ksharp.common.io.*
import org.ksharp.typesystem.types.Labeled
import org.ksharp.typesystem.types.Type

class LabeledSerializer : SerializerWriter<Labeled>, SerializerReader<Labeled> {
    override fun write(input: Labeled, buffer: BufferWriter, table: BinaryTable) {
        buffer.add(table.add(input.label))
        input.type.writeTo(buffer, table)
    }

    override fun read(buffer: BufferView, table: BinaryTableView): Labeled {
        val label = table[buffer.readInt(0)]
        val type = buffer.bufferFrom(4).readType<Type>(table)
        return Labeled(label, type)
    }
}