package org.ksharp.typesystem.serializer

import org.ksharp.common.io.*
import org.ksharp.common.mapBuilder
import org.ksharp.common.put
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.TypeSystemImpl
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
    FunctionType(FunctionSerializer()),
    IntersectionType(IntersectionSerializer()),
    TupleType(TupleSerializer()),
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

fun TypeSystem.writeTo(buffer: BufferWriter, table: BinaryTable) {
    buffer.add(size)
    asSequence().forEach { (name, type) ->
        buffer.add(table.add(name))
        type.writeTo(buffer, table)
    }
}

fun readTypeSystemFrom(buffer: BufferView, table: BinaryTableView): TypeSystem {
    val paramsSize = buffer.readInt(0)
    val types = mapBuilder<String, Type>()
    var position = 4
    repeat(paramsSize) {
        val typeBuffer = buffer.bufferFrom(position)
        val key = table[typeBuffer.readInt(0)]
        position = typeBuffer.readInt(4) - buffer.offset
        types.put(
            key,
            readTypeFrom(typeBuffer.bufferFrom(4), table)
        )
    }
    return TypeSystemImpl(null, types.build())
}