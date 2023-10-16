package org.ksharp.module.prelude.serializer

import org.ksharp.common.HandlePromise
import org.ksharp.common.io.*
import org.ksharp.module.prelude.types.CharType
import org.ksharp.module.prelude.types.charType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.serializer.TypeSerializerReader

class CharTypeSerializer : SerializerWriter<CharType>, TypeSerializerReader<CharType> {
    override fun read(handle: HandlePromise<TypeSystem>, buffer: BufferView, table: BinaryTableView): CharType =
        charType

    override fun write(input: CharType, buffer: BufferWriter, table: BinaryTable) {
        //This type is static, so just the default serializer header is enough
    }
}
