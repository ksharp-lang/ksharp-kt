package org.ksharp.typesystem.serializer

import org.ksharp.common.io.*
import org.ksharp.typesystem.types.TypeConstructor

class NoSerializer : SerializerWriter<TypeConstructor>, SerializerReader<TypeConstructor> {
    override fun write(input: TypeConstructor, buffer: BufferWriter, table: BinaryTable) {

    }

    override fun read(buffer: BufferView, table: BinaryTableView): TypeConstructor =
        TODO("No supported")
}