package org.ksharp.module.prelude.serializer

import org.ksharp.common.io.*
import org.ksharp.module.prelude.types.CharType
import org.ksharp.module.prelude.types.charType

class CharTypeSerializer : SerializerWriter<CharType>, SerializerReader<CharType> {
    override fun read(buffer: BufferView, table: BinaryTableView): CharType = charType

    override fun write(input: CharType, buffer: BufferWriter, table: BinaryTable) {
    }
}