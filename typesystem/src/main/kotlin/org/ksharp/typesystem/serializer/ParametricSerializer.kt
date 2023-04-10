package org.ksharp.typesystem.serializer

import org.ksharp.common.io.*
import org.ksharp.typesystem.types.Parameter
import org.ksharp.typesystem.types.ParametricType
import org.ksharp.typesystem.types.TypeVariable

class ParameterSerializer : SerializerWriter<Parameter>, SerializerReader<Parameter> {
    override fun write(input: Parameter, buffer: BufferWriter, table: BinaryTable) {
        buffer.add(table.add(input.name))
    }

    override fun read(buffer: BufferView, table: BinaryTableView): Parameter =
        Parameter(table[buffer.readInt(0)])

}

class ParametricTypeSerializer : SerializerWriter<ParametricType>, SerializerReader<ParametricType> {
    override fun write(input: ParametricType, buffer: BufferWriter, table: BinaryTable) {
        input.type.writeTo(buffer, table)
        input.params.writeTo(buffer, table)
    }

    override fun read(buffer: BufferView, table: BinaryTableView): ParametricType {
        val paramsOffset = buffer.readInt(0)
        val type = readTypeFrom<TypeVariable>(buffer, table)
        val types = buffer
            .bufferFrom(paramsOffset - buffer.offset)
            .readListOfTypes(table)
        return ParametricType(type, types)
    }
}