package org.ksharp.typesystem.serializer

import org.ksharp.common.cast
import org.ksharp.common.io.*
import org.ksharp.typesystem.types.TraitType

class MethodTypeSerializer : SerializerWriter<TraitType.MethodType>, SerializerReader<TraitType.MethodType> {
    override fun write(input: TraitType.MethodType, buffer: BufferWriter, table: BinaryTable) {
        buffer.writeTypeVisibility(input)
        buffer.add(table.add(input.name))
        input.arguments.writeTo(buffer, table)
    }

    override fun read(buffer: BufferView, table: BinaryTableView): TraitType.MethodType {
        val visibility = buffer.readTypeVisibility(0)
        val name = table[buffer.readInt(4)]
        val arguments = buffer.bufferFrom(8).readListOfTypes(table)
        return TraitType.MethodType(visibility, name, arguments)
    }

}

class TraitSerializer : SerializerWriter<TraitType>, SerializerReader<TraitType> {
    override fun write(input: TraitType, buffer: BufferWriter, table: BinaryTable) {
        buffer.writeTypeVisibility(input)
        buffer.add(table.add(input.name))
        buffer.add(table.add(input.param))
        input.methods.writeTo(buffer, table)
    }

    override fun read(buffer: BufferView, table: BinaryTableView): TraitType {
        val visibility = buffer.readTypeVisibility(0)
        val name = table[buffer.readInt(4)]
        val param = table[buffer.readInt(8)]
        val methods = buffer.bufferFrom(12).readMapOfTypes(table)
        return TraitType(visibility, name, param, methods.cast())
    }
}