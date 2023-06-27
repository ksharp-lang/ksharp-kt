package org.ksharp.typesystem.serializer

import org.ksharp.common.io.*
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
    TypeAlias(TypeAliasSerializer()),
    Parameter(ParameterSerializer()),
    ParametricType(ParametricTypeSerializer()),
    Labeled(LabeledSerializer()),
    FunctionType(FunctionSerializer()),
    IntersectionType(IntersectionSerializer()),
    TupleType(TupleSerializer()),
    ClassType(ClassTypeSerializer()),
    UnionType(UnionTypeSerializer()),
    MethodType(MethodTypeSerializer()),
    NoType(TypeConstructorSerializer()),
    TraitType(TraitSerializer())
}

@Suppress("UNCHECKED_CAST")
fun Type.writeTo(buffer: BufferWriter, table: BinaryTable) {
    val serializer = this.serializer.writer as SerializerWriter<Type>
    val writerType = serializer.javaClass.name
    newBufferWriter().apply {
        add(0)
        add(table.add(writerType))
        serializer.write(this@writeTo, this, table)
        set(0, size)
        transferTo(buffer)
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T> BufferView.readType(table: BinaryTableView): T {
    val serializerName = table[readInt(4)]
    val serializer = Class.forName(serializerName)
        .getDeclaredConstructor()
        .newInstance() as SerializerReader<T>
    return serializer.read(bufferFrom(8), table)
}

fun TypeSystem.writeTo(buffer: BufferWriter, table: BinaryTable) {
    asSequence().writeTo(size, buffer, table)
}

fun BufferView.readTypeSystem(table: BinaryTableView, parent: TypeSystem? = null): TypeSystem {
    return TypeSystemImpl(parent, readMapOfTypes(table))
}
