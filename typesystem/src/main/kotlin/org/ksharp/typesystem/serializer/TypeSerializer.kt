package org.ksharp.typesystem.serializer

import org.ksharp.common.io.*
import org.ksharp.typesystem.types.Type

interface TypeSerializer {
    val writer: SerializerWriter<out Type>
}

enum class TypeSerializers(
    override val writer: SerializerWriter<out Type>
) : TypeSerializer {
    Concrete(ConcreteSerializer()),
    Alias(AliasSerializer()),
    Parameter(ParameterSerializer()),
    ParametricType(ParametricTypeSerializer()),
    Labeled(LabeledSerializer()),
    NoDefined(object : SerializerWriter<Type> {
        override fun write(input: Type, buffer: BufferWriter, table: BinaryTable) {
            TODO("Not yet implemented")
        }
    })
}

@Suppress("UNCHECKED_CAST")
fun Type.writeTo(buffer: BufferWriter, table: BinaryTable) {
    val serializer = this.serializer.writer as SerializerWriter<Type>
    val writerType = serializer.javaClass.name
    val bufferStartPosition = buffer.size
    buffer.add(0)
    buffer.add(table.add(writerType))
    serializer.write(this, buffer, table)
    buffer.set(bufferStartPosition, buffer.size)
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T> readTypeFrom(buffer: BufferView, table: BinaryTableView): T {
    val serializerName = table[buffer.readInt(4)]
    val serializer = Class.forName(serializerName)
        .getDeclaredConstructor()
        .newInstance() as SerializerReader<T>
    return serializer.read(buffer.bufferFrom(8), table)
}