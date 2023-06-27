package org.ksharp.typesystem.serializer

import org.ksharp.common.cast
import org.ksharp.common.io.*
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo
import org.ksharp.typesystem.types.UnionType

class ClassTypeSerializer : SerializerWriter<UnionType.ClassType>, SerializerReader<UnionType.ClassType> {
    override fun read(buffer: BufferView, table: BinaryTableView): UnionType.ClassType {
        val label = table[buffer.readInt(0)]
        val types = buffer.bufferFrom(4).readListOfTypes(table)
        return UnionType.ClassType(label, types)
    }

    override fun write(input: UnionType.ClassType, buffer: BufferWriter, table: BinaryTable) {
        buffer.add(table.add(input.label))
        input.params.writeTo(buffer, table)
    }
}

class UnionTypeSerializer : SerializerWriter<UnionType>, SerializerReader<UnionType> {
    override fun read(buffer: BufferView, table: BinaryTableView): UnionType =
        UnionType(buffer.readAttributes(table), buffer.bufferFrom(buffer.readInt(0)).readMapOfTypes(table).cast())

    override fun write(input: UnionType, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        input.arguments.writeTo(buffer, table)
    }
}
