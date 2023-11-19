package org.ksharp.typesystem.serializer

import org.ksharp.common.HandlePromise
import org.ksharp.common.io.*
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.FixedTraitType
import org.ksharp.typesystem.types.ImplType
import org.ksharp.typesystem.types.TraitType
import org.ksharp.typesystem.types.Type

class ImplTypeSerializer : SerializerWriter<ImplType>, TypeSerializerReader<ImplType> {
    override fun write(input: ImplType, buffer: BufferWriter, table: BinaryTable) {
        input.trait.writeTo(buffer, table)
        input.impl.writeTo(buffer, table)
    }

    override fun read(handle: HandlePromise<TypeSystem>, buffer: BufferView, table: BinaryTableView): ImplType {
        val offset = buffer.readInt(0)
        val trait = buffer.readType<TraitType>(handle, table)
        val impl = buffer.bufferFrom(offset).readType<Type>(handle, table)
        return ImplType(trait, impl)
    }
}

class FixedTraitTypeSerializer : SerializerWriter<FixedTraitType>, TypeSerializerReader<FixedTraitType> {
    override fun write(input: FixedTraitType, buffer: BufferWriter, table: BinaryTable) {
        input.trait.writeTo(buffer, table)
    }

    override fun read(handle: HandlePromise<TypeSystem>, buffer: BufferView, table: BinaryTableView): FixedTraitType {
        val trait = buffer.readType<TraitType>(handle, table)
        return FixedTraitType(trait)
    }
}
