package org.ksharp.typesystem.serializer

import org.ksharp.common.cast
import org.ksharp.common.io.*
import org.ksharp.typesystem.types.UnionType

class ClassTypeSerializer : SerializerWriter<UnionType.ClassType>, SerializerReader<UnionType.ClassType> {
    override fun read(buffer: BufferView, table: BinaryTableView): UnionType.ClassType {
        val visibility = buffer.readTypeVisibility(0)
        val label = table[buffer.readInt(4)]
        val types = buffer.bufferFrom(8).readListOfTypes(table)
        return UnionType.ClassType(visibility, label, types)
    }

    override fun write(input: UnionType.ClassType, buffer: BufferWriter, table: BinaryTable) {
        buffer.writeTypeVisibility(input)
        buffer.add(table.add(input.label))
        input.params.writeTo(buffer, table)
    }
}

class UnionTypeSerializer : SerializerWriter<UnionType>, SerializerReader<UnionType> {
    override fun read(buffer: BufferView, table: BinaryTableView): UnionType =
        UnionType(buffer.readTypeVisibility(0), buffer.bufferFrom(4).readMapOfTypes(table).cast())

    override fun write(input: UnionType, buffer: BufferWriter, table: BinaryTable) {
        buffer.writeTypeVisibility(input)
        input.arguments.writeTo(buffer, table)
    }
}