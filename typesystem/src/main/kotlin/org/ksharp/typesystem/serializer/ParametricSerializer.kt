package org.ksharp.typesystem.serializer

import org.ksharp.common.io.*
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo
import org.ksharp.typesystem.types.Parameter
import org.ksharp.typesystem.types.ParametricType
import org.ksharp.typesystem.types.TypeVariable

class ParameterSerializer : SerializerWriter<Parameter>, SerializerReader<Parameter> {
    override fun write(input: Parameter, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        buffer.add(table.add(input.name))
    }

    override fun read(buffer: BufferView, table: BinaryTableView): Parameter =
        Parameter(buffer.readAttributes(table), table[buffer.readInt(buffer.readInt(0))])

}

class ParametricTypeSerializer : SerializerWriter<ParametricType>, SerializerReader<ParametricType> {
    override fun write(input: ParametricType, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        input.type.writeTo(buffer, table)
        input.params.writeTo(buffer, table)
    }

    override fun read(buffer: BufferView, table: BinaryTableView): ParametricType {
        val offset = buffer.readInt(0)
        val attributes = buffer.readAttributes(table)
        val paramsOffset = buffer.readInt(offset) + offset
        val type = buffer.bufferFrom(offset).readType<TypeVariable>(table)
        val types = buffer
            .bufferFrom(paramsOffset)
            .readListOfTypes(table)
        return ParametricType(attributes, type, types)
    }
}
