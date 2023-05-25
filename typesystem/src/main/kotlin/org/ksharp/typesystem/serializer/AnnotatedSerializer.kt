package org.ksharp.typesystem.serializer

import org.ksharp.common.io.*
import org.ksharp.typesystem.annotations.readAnnotations
import org.ksharp.typesystem.annotations.writeTo
import org.ksharp.typesystem.types.Annotated

class AnnotatedSerializer : SerializerWriter<Annotated>, SerializerReader<Annotated> {

    override fun write(input: Annotated, buffer: BufferWriter, table: BinaryTable) {
        input.annotations.writeTo(buffer, table)
        input.type.writeTo(buffer, table)
    }

    override fun read(buffer: BufferView, table: BinaryTableView): Annotated {
        val typePos = buffer.readInt(0)
        val annotations = buffer.readAnnotations(table)
        return Annotated(annotations, buffer.bufferFrom(typePos).readType(table))
    }
}
