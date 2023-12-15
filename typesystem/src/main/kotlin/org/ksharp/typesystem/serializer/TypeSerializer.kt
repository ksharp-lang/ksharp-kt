package org.ksharp.typesystem.serializer

import org.ksharp.common.HandlePromise
import org.ksharp.common.handlePromise
import org.ksharp.common.io.*
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.TypeSystemImpl
import org.ksharp.typesystem.types.Type

typealias TypeSerializerResolver = (ordinal: Int) -> TypeSerializer

fun interface TypeSerializerReader<T : Type> {
    fun read(handle: HandlePromise<TypeSystem>, buffer: BufferView, table: BinaryTableView): T
}

private val catalogs = mutableMapOf<String, TypeSerializerResolver>(
    "default" to { ordinal -> TypeSerializers.entries[ordinal] }
)

fun registerCatalog(name: String, resolver: TypeSerializerResolver) {
    require(!catalogs.containsKey(name)) { "Catalog $name already registered" }
    catalogs[name] = resolver
}

@Suppress("UNCHECKED_CAST")
fun <T : Type> typeSerializeReaderByCatalog(catalog: String, ordinal: Int) =
    catalogs[catalog]!!(ordinal).writer as TypeSerializerReader<T>

interface TypeSerializer {
    val writer: SerializerWriter<out Type>
    val catalog: String
    val ordinal: Int
}

enum class TypeSerializers(
    override val writer: SerializerWriter<out Type>,
    override val catalog: String = "default"
) : TypeSerializer {
    //ADD new Serializers at the end of the list
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
    val serializer = this.serializer
    val writer = serializer.writer as SerializerWriter<Type>
    newBufferWriter().apply {
        add(0)
        add(table.add(serializer.catalog))
        add(serializer.ordinal)
        writer.write(this@writeTo, this, table)
        set(0, size)
        transferTo(buffer)
    }
}

inline fun <reified T : Type> BufferView.readType(typeSystem: HandlePromise<TypeSystem>, table: BinaryTableView): T {
    val catalog = table[readInt(4)]
    val ordinal = readInt(8)
    val serializer = typeSerializeReaderByCatalog<T>(catalog, ordinal)
    return serializer.read(typeSystem, bufferFrom(12), table)
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
