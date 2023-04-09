package org.ksharp.typesystem.serializer

import org.ksharp.common.io.*
import org.ksharp.typesystem.types.Concrete

class ConcreteSerializer : SerializerWriter<Concrete>, SerializerReader<Concrete> {
    override fun write(input: Concrete, buffer: BufferWriter, table: BinaryTable) {
        buffer.add(table.add(input.name))
    }

    override fun read(buffer: BufferView, table: BinaryTableView): Concrete =
        Concrete(table[buffer.readInt(0)])

}