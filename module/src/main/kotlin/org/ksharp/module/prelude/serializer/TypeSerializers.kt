package org.ksharp.module.prelude.serializer

import org.ksharp.common.io.SerializerWriter
import org.ksharp.typesystem.serializer.TypeSerializer
import org.ksharp.typesystem.types.Type

enum class TypeSerializers(
    override val writer: SerializerWriter<out Type>
) : TypeSerializer {
    CharType(CharTypeSerializer()),
    NumericType(NumericTypeSerializer())
}