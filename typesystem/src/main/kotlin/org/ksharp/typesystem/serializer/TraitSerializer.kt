package org.ksharp.typesystem.serializer

import org.ksharp.common.cast
import org.ksharp.common.io.*
import org.ksharp.typesystem.types.TraitType

class MethodTypeSerializer : SerializerWriter<TraitType.MethodType>, SerializerReader<TraitType.MethodType> {
    override fun write(input: TraitType.MethodType, buffer: BufferWriter, table: BinaryTable) {
        buffer.add(table.add(input.name))
        input.arguments.writeTo(buffer, table)
    }

    override fun read(buffer: BufferView, table: BinaryTableView): TraitType.MethodType {
        val name = table[buffer.readInt(0)]
        val arguments = buffer.bufferFrom(4).readListOfTypes(table)
        return TraitType.MethodType(name, arguments)
    }

}

class TraitSerializer : SerializerWriter<TraitType>, SerializerReader<TraitType> {
    override fun write(input: TraitType, buffer: BufferWriter, table: BinaryTable) {
        buffer.add(table.add(input.name))
        buffer.add(table.add(input.param))
        input.methods.writeTo(buffer, table)
    }

    override fun read(buffer: BufferView, table: BinaryTableView): TraitType {
        val name = table[buffer.readInt(0)]
        val param = table[buffer.readInt(4)]
        val methods = buffer.bufferFrom(8).readMapOfTypes(table)
        return TraitType(name, param, methods.cast())
    }
}