package org.ksharp.typesystem.serializer

import org.ksharp.common.HandlePromise
import org.ksharp.common.io.*
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.Labeled
import org.ksharp.typesystem.types.Type

class LabeledSerializer : SerializerWriter<Labeled>, TypeSerializerReader<Labeled> {
    override fun write(input: Labeled, buffer: BufferWriter, table: BinaryTable) {
        buffer.add(table.add(input.label))
        input.type.writeTo(buffer, table)
    }

    override fun read(handle: HandlePromise<TypeSystem>, buffer: BufferView, table: BinaryTableView): Labeled {
        val label = table[buffer.readInt(0)]
        val type = buffer.bufferFrom(4).readType<Type>(handle, table)
        return Labeled(label, type)
    }
}
