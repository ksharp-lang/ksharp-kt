package org.ksharp.module.prelude.serializer

import org.ksharp.common.HandlePromise
import org.ksharp.common.io.*
import org.ksharp.module.prelude.types.Numeric
import org.ksharp.module.prelude.types.NumericType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.serializer.TypeSerializerReader

class NumericTypeSerializer : SerializerWriter<NumericType>, TypeSerializerReader<NumericType> {
    override fun write(input: NumericType, buffer: BufferWriter, table: BinaryTable) {
        buffer.add(table.add(input.type.name))
    }

    override fun read(handle: HandlePromise<TypeSystem>, buffer: BufferView, table: BinaryTableView): NumericType {
        val typeName = table[buffer.readInt(0)]
        return NumericType(Numeric.valueOf(typeName))
    }
}
