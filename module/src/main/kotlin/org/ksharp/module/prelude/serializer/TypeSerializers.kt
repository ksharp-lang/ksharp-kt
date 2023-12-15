package org.ksharp.module.prelude.serializer

import org.ksharp.common.io.SerializerWriter
import org.ksharp.typesystem.serializer.TypeSerializer
import org.ksharp.typesystem.types.Type

enum class TypeSerializers(
    override val writer: SerializerWriter<out Type>,
    override val catalog: String = "prelude"
) : TypeSerializer {
    //Add new serializers at the end of the list
    CharType(CharTypeSerializer()),
    NumericType(NumericTypeSerializer())
}
