package org.ksharp.ir.serializer

import org.ksharp.common.*
import org.ksharp.common.io.*
import org.ksharp.ir.*
import org.ksharp.module.bytecode.StringPoolBuilder
import org.ksharp.module.bytecode.StringPoolView
import java.io.OutputStream

interface IrNodeSerializer<S : IrNode> : SerializerWriter<S>, SerializerReader<S>

enum class IrNodeSerializers(
    val serializer: IrNodeSerializer<out IrNode>,
) {
    //ADD new Serializers at the end of the list
    Module(IrModuleSerializer()),
    Function(IrFunctionSerializer()),
    Integer(IrIntegerSerializer()),
    Decimal(IrDecimalSerializer()),
    Character(IrCharacterSerializer()),
    String(IrStringSerializer()),
    Bool(IrBoolSerializer()),
    NumCast(IrNumCastSerializer()),
    Pair(IrPairSerializer()),
    List(IrCollectionsSerializer(::IrList)),
    Set(IrCollectionsSerializer(::IrSet)),
    Map(IrMapSerializer()),
    Sum(IrBinaryOperationSerializer(::IrSum)),
    Sub(IrBinaryOperationSerializer(::IrSub)),
    Mul(IrBinaryOperationSerializer(::IrMul)),
    Div(IrBinaryOperationSerializer(::IrDiv)),
    Pow(IrBinaryOperationSerializer(::IrPow)),
    Mod(IrBinaryOperationSerializer(::IrMod)),
    ArithmeticCall(IrArithmeticCallSerializer()),
    Lt(IrBinaryOperationSerializer(::IrLt)),
    Le(IrBinaryOperationSerializer(::IrLe)),
    Ge(IrBinaryOperationSerializer(::IrGe)),
    Gt(IrBinaryOperationSerializer(::IrGt)),
    Eq(IrBinaryOperationSerializer(::IrEq)),
    NotEq(IrBinaryOperationSerializer(::IrNotEq)),
    Arg(IrVarValueSerializer(::IrArg)),
    Var(IrVarValueSerializer(::IrVar)),
    If(IrIfSerializer()),
    Call(IrCallSerializer()),
    Let(IrLetSerializer()),
    LetSetVar(IrSetVarSerializer()),

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
        .serializer.read(
            bufferFrom(8), table
        )
}

fun List<IrNode>.writeTo(buffer: BufferWriter, table: BinaryTable) {
    buffer.add(size)
    forEach {
        it.serialize(buffer, table)
    }
}

fun BufferView.readListOfNodes(tableView: BinaryTableView): List<IrNode> {
    val paramsSize = readInt(0)
    val result = listBuilder<IrNode>()
    var position = 4
    repeat(paramsSize) {
        val typeBuffer = bufferFrom(position)
        position += typeBuffer.readInt(0)
        result.add(typeBuffer.readIrNode(tableView))
    }
    return result.build()
}

fun IrModule.writeTo(output: OutputStream) {
    val stringPool = StringPoolBuilder()
    val code = newBufferWriter().apply {
        serialize(this, stringPool)
    }
    val header = newBufferWriter()
    header.add(stringPool.size) // 0
    header.transferTo(output)
    stringPool.writeTo(output)
    code.transferTo(output)
}


fun BufferView.readIrModule(): IrModule {
    val stringPoolSize = readInt(0)
    val offset = 4
    val stringPool = StringPoolView(bufferFrom(offset))
    return bufferFrom(offset + stringPoolSize).readIrNode(stringPool).cast()
}
