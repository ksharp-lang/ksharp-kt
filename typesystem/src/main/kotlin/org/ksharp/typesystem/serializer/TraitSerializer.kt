package org.ksharp.typesystem.serializer

import org.ksharp.common.HandlePromise
import org.ksharp.common.cast
import org.ksharp.common.io.*
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo
import org.ksharp.typesystem.types.TraitType

class MethodTypeSerializer : SerializerWriter<TraitType.MethodType>, TypeSerializerReader<TraitType.MethodType> {
    override fun write(input: TraitType.MethodType, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        buffer.add(table.add(input.name))
        buffer.add(if (input.withDefaultImpl) 1 else 0)
        input.arguments.writeTo(buffer, table)
    }

    override fun read(
        handle: HandlePromise<TypeSystem>,
        buffer: BufferView,
        table: BinaryTableView
    ): TraitType.MethodType {
        val offset = buffer.readInt(0)
        val attributes = buffer.readAttributes(table)
        val name = table[buffer.readInt(offset)]
        val withDefaultImpl = buffer.readInt(offset + 4) == 1
        val arguments = buffer.bufferFrom(8 + offset).readListOfTypes(handle, table)
        return TraitType.MethodType(handle, attributes, name, arguments, withDefaultImpl)
    }

}

class TraitSerializer : SerializerWriter<TraitType>, TypeSerializerReader<TraitType> {
    override fun write(input: TraitType, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        buffer.add(table.add(input.module))
        buffer.add(table.add(input.name))
        buffer.add(table.add(input.param))
        input.methods.writeTo(buffer, table)
    }

    override fun read(handle: HandlePromise<TypeSystem>, buffer: BufferView, table: BinaryTableView): TraitType {
        var offset = buffer.readInt(0)
        val attributes = buffer.readAttributes(table)
        val module = table[buffer.readInt(offset)]
        offset += 4
        val name = table[buffer.readInt(offset)]
        offset += 4
        val param = table[buffer.readInt(offset)]
        offset += 4
        val methods = buffer.bufferFrom(offset).readMapOfTypes(handle, table)
        return TraitType(handle, attributes, module, name, param, methods.cast())
    }
}
