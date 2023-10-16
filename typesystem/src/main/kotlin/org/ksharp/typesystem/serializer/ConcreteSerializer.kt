package org.ksharp.typesystem.serializer

import org.ksharp.common.HandlePromise
import org.ksharp.common.io.*
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo
import org.ksharp.typesystem.types.Concrete

class ConcreteSerializer : SerializerWriter<Concrete>, TypeSerializerReader<Concrete> {
    override fun write(input: Concrete, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        buffer.add(table.add(input.name))
    }

    override fun read(handle: HandlePromise<TypeSystem>, buffer: BufferView, table: BinaryTableView): Concrete =
        Concrete(handle, buffer.readAttributes(table), table[buffer.readInt(buffer.readInt(0))])

}
