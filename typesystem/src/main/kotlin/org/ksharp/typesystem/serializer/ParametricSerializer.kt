package org.ksharp.typesystem.serializer

import org.ksharp.common.io.*
import org.ksharp.typesystem.types.Parameter
import org.ksharp.typesystem.types.ParametricType
import org.ksharp.typesystem.types.TypeVariable

class ParameterSerializer : SerializerWriter<Parameter>, SerializerReader<Parameter> {
    override fun write(input: Parameter, buffer: BufferWriter, table: BinaryTable) {
        buffer.writeTypeVisibility(input)
        buffer.add(table.add(input.name))
    }

    override fun read(buffer: BufferView, table: BinaryTableView): Parameter =
        Parameter(buffer.readTypeVisibility(0), table[buffer.readInt(4)])

}

class ParametricTypeSerializer : SerializerWriter<ParametricType>, SerializerReader<ParametricType> {
    override fun write(input: ParametricType, buffer: BufferWriter, table: BinaryTable) {
        buffer.writeTypeVisibility(input)
        input.type.writeTo(buffer, table)
        input.params.writeTo(buffer, table)
    }

    override fun read(buffer: BufferView, table: BinaryTableView): ParametricType {
        val visibility = buffer.readTypeVisibility(0)
        val paramsOffset = buffer.readInt(4)
        val type = buffer.bufferFrom(4).readType<TypeVariable>(table)
        val types = buffer
            .bufferFrom(4 + paramsOffset)
            .readListOfTypes(table)
        return ParametricType(visibility, type, types)
    }
}