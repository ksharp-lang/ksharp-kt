package org.ksharp.typesystem.serializer

import org.ksharp.common.io.*
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo
import org.ksharp.typesystem.types.Concrete

class ConcreteSerializer : SerializerWriter<Concrete>, SerializerReader<Concrete> {
    override fun write(input: Concrete, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        buffer.add(table.add(input.name))
    }

    override fun read(buffer: BufferView, table: BinaryTableView): Concrete =
        Concrete(buffer.readAttributes(table), table[buffer.readInt(buffer.readInt(0))])

}
