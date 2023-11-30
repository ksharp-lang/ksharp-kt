package org.ksharp.typesystem.serializer

import org.ksharp.common.HandlePromise
import org.ksharp.common.handlePromise
import org.ksharp.common.io.*
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.TypeSystemImpl
import org.ksharp.typesystem.types.Type

fun interface TypeSerializerReader<T : Type> {
    fun read(handle: HandlePromise<TypeSystem>, buffer: BufferView, table: BinaryTableView): T
}

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
    PartialFunctionType(PartialFunctionSerializer()),
    IntersectionType(IntersectionSerializer()),
    TupleType(TupleSerializer()),
    ClassType(ClassTypeSerializer()),
    UnionType(UnionTypeSerializer()),
    MethodType(MethodTypeSerializer()),
    ConstructorType(TypeConstructorSerializer()),
    TraitType(TraitSerializer()),
    FixedTraitType(FixedTraitTypeSerializer()),
    ImplType(ImplTypeSerializer()),
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
inline fun <reified T : Type> BufferView.readType(typeSystem: HandlePromise<TypeSystem>, table: BinaryTableView): T {
    val serializerName = table[readInt(4)]
    val serializer = Class.forName(serializerName)
        .getDeclaredConstructor()
        .newInstance() as TypeSerializerReader<T>
    return serializer.read(typeSystem, bufferFrom(8), table)
}

fun TypeSystem.writeTo(buffer: BufferWriter, table: BinaryTable) {
    asSequence().writeTo(size, buffer, table)
}

fun BufferView.readTypeSystem(
    table: BinaryTableView,
    parent: TypeSystem? = null,
    handle: HandlePromise<TypeSystem> = handlePromise()
): TypeSystem {
    return TypeSystemImpl(parent, handle, readMapOfTypes(handle, table)).also {
        handle.set(it)
    }
}
