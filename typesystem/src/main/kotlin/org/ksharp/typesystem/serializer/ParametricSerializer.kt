package org.ksharp.typesystem.serializer

import org.ksharp.common.HandlePromise
import org.ksharp.common.io.*
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo
import org.ksharp.typesystem.types.*

class ParameterSerializer : SerializerWriter<Parameter>, TypeSerializerReader<Parameter> {
    override fun write(input: Parameter, buffer: BufferWriter, table: BinaryTable) {
        buffer.add(table.add(input.name))
    }

    override fun read(handle: HandlePromise<TypeSystem>, buffer: BufferView, table: BinaryTableView): Parameter =
        Parameter(handle, table[buffer.readInt(0)])

}

class ParametricTypeSerializer : SerializerWriter<ParametricType>, TypeSerializerReader<ParametricType> {
    override fun write(input: ParametricType, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        val type = input.type
        if (type is TraitType) {
            Alias(type.typeSystem, type.name).writeTo(buffer, table)
        } else input.type.writeTo(buffer, table)
        input.params.writeTo(buffer, table)
    }

    override fun read(handle: HandlePromise<TypeSystem>, buffer: BufferView, table: BinaryTableView): ParametricType {
        val offset = buffer.readInt(0)
        val attributes = buffer.readAttributes(table)
        val paramsOffset = buffer.readInt(offset) + offset
        val type = buffer.bufferFrom(offset).readType<TypeVariable>(handle, table)
        val types = buffer
            .bufferFrom(paramsOffset)
            .readListOfTypes(handle, table)
        return ParametricType(handle, attributes, type, types)
    }
}
