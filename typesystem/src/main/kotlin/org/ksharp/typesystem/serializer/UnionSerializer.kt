package org.ksharp.typesystem.serializer

import org.ksharp.common.HandlePromise
import org.ksharp.common.cast
import org.ksharp.common.io.*
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo
import org.ksharp.typesystem.types.UnionType

class ClassTypeSerializer : SerializerWriter<UnionType.ClassType>, TypeSerializerReader<UnionType.ClassType> {
    override fun read(
        handle: HandlePromise<TypeSystem>,
        buffer: BufferView,
        table: BinaryTableView
    ): UnionType.ClassType {
        val label = table[buffer.readInt(0)]
        val types = buffer.bufferFrom(4).readListOfTypes(handle, table)
        return UnionType.ClassType(handle, label, types)
    }

    override fun write(input: UnionType.ClassType, buffer: BufferWriter, table: BinaryTable) {
        buffer.add(table.add(input.label))
        input.params.writeTo(buffer, table)
    }
}

class UnionTypeSerializer : SerializerWriter<UnionType>, TypeSerializerReader<UnionType> {
    override fun read(handle: HandlePromise<TypeSystem>, buffer: BufferView, table: BinaryTableView): UnionType =
        UnionType(
            handle,
            buffer.readAttributes(table),
            buffer.bufferFrom(buffer.readInt(0)).readMapOfTypes(handle, table).cast()
        )

    override fun write(input: UnionType, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        input.arguments.writeTo(buffer, table)
    }
}
