package org.ksharp.ir.serializer

import org.ksharp.common.Line
import org.ksharp.common.Location
import org.ksharp.common.Offset
import org.ksharp.common.cast
import org.ksharp.common.io.*
import org.ksharp.ir.*

interface IrNodeSerializer<S : IrNode> : SerializerWriter<S>, SerializerReader<S>

enum class IrNodeSerializers(
    val serializer: IrNodeSerializer<out IrNode>,
) {
    //ADD new Serializers at the end of the list
    NoDefined(object : IrNodeSerializer<IrNode> {
        override fun write(input: IrNode, buffer: BufferWriter, table: BinaryTable) {
            error("No serializer for $input")
        }

        override fun read(buffer: BufferView, table: BinaryTableView): IrNode {
            error("No serializer for IrNode")
        }
    }),

    Sum(IrBinaryOperationSerializer(::IrSum)),
    Sub(IrBinaryOperationSerializer(::IrSub)),
    Mul(IrBinaryOperationSerializer(::IrMul)),
    Div(IrBinaryOperationSerializer(::IrDiv)),
    Pow(IrBinaryOperationSerializer(::IrPow)),
    Mod(IrBinaryOperationSerializer(::IrMod)),

}

fun Location.writeTo(buffer: BufferWriter) {
    buffer.add(start.first.value)
    buffer.add(start.second.value)
    buffer.add(end.first.value)
    buffer.add(end.second.value)
}

fun BufferView.readLocation(): Location {
    val startLine = Line(readInt(0))
    val startOffset = Offset(readInt(4))
    val endLine = Line(readInt(8))
    val endOffset = Offset(readInt(12))
    return Location(startLine to startOffset, endLine to endOffset)
}

fun IrNode.serialize(buffer: BufferWriter, table: BinaryTable) {
    val ordinal = serializer.ordinal
    newBufferWriter().apply {
        add(0)
        add(ordinal)
        serializer.serializer.cast<SerializerWriter<IrNode>>()
            .write(this@serialize, this, table)
        set(0, size)
        transferTo(buffer)
    }
}

fun BufferView.readIrNode(table: BinaryTableView): IrNode {
    val serializerIndex = readInt(4)
    return IrNodeSerializers.entries[serializerIndex]
        .serializer.read(bufferFrom(8), table)
}
