package org.ksharp.ir.serializer

import org.ksharp.common.*
import org.ksharp.common.io.*
import org.ksharp.ir.*
import org.ksharp.module.Impl
import org.ksharp.module.bytecode.StringPoolBuilder
import org.ksharp.module.bytecode.StringPoolView
import org.ksharp.module.bytecode.readImpl
import org.ksharp.module.bytecode.writeTo
import org.ksharp.module.prelude.preludeModule
import java.io.OutputStream

interface IrNodeSerializer<S : IrNode> : SerializerWriter<S> {
    fun read(lookup: FunctionLookup, loader: LoadIrModuleFn, buffer: BufferView, table: BinaryTableView): S
}

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
    Equals(IrBinaryOperationSerializer(::IrEquals)),
    NotEquals(IrBinaryOperationSerializer(::IrNotEquals)),
    BitAnd(IrBinaryOperationSerializer(::IrBitAnd)),
    BitOr(IrBinaryOperationSerializer(::IrBitOr)),
    BitXor(IrBinaryOperationSerializer(::IrBitXor)),
    BitShr(IrBinaryOperationSerializer(::IrBitShr)),
    BitShl(IrBinaryOperationSerializer(::IrBitShl)),
    Arg(IrVarValueSerializer(::IrArg)),
    Var(IrVarValueSerializer(::IrVar)),
    If(IrIfSerializer()),
    Call(IrCallSerializer()),
    NativeCall(IrNativeCallSerializer()),
    Let(IrLetSerializer()),
    LetSetVar(IrSetVarSerializer()),
    ModuleCall(IrModuleCallSerializer()),
    Comparable(IrComparableSerializer()),
    ToString(IrToStringSerializer()),
    LambdaCall(IrLambdaCallSerializer()),
    Lambda(IrLambdaSerializer()),
    CaptureVar(IrCaptureVarSerializer())
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

fun BufferView.readIrNode(lookup: FunctionLookup, loader: LoadIrModuleFn, table: BinaryTableView): IrNode {
    val serializerIndex = readInt(4)
    return IrNodeSerializers.entries[serializerIndex]
        .serializer
        .read(
            lookup,
            loader,
            bufferFrom(8),
            table
        )
}

fun List<IrNode>.writeTo(buffer: BufferWriter, table: BinaryTable) {
    buffer.add(size)
    forEach {
        it.serialize(buffer, table)
    }
}

fun Map<String, List<IrNode>>.writeTo(buffer: BufferWriter, table: BinaryTable) {
    buffer.add(size)
    forEach {
        buffer.add(table.add(it.key))
        it.value.writeTo(buffer, table)
    }
}

@JvmName("writeMapOfImplNodes")
fun Map<Impl, List<IrNode>>.writeTo(buffer: BufferWriter, table: BinaryTable) {
    buffer.add(size)
    forEach {
        it.key.writeTo(buffer, table)
        it.value.writeTo(buffer, table)
    }
}

fun BufferView.readListOfNodes(
    lookup: FunctionLookup,
    loader: LoadIrModuleFn,
    tableView: BinaryTableView
): Pair<Int, List<IrNode>> {
    val paramsSize = readInt(0)
    val result = listBuilder<IrNode>()
    var position = 4
    repeat(paramsSize) {
        val typeBuffer = bufferFrom(position)
        position += typeBuffer.readInt(0)
        result.add(typeBuffer.readIrNode(lookup, loader, tableView))
    }
    return position to result.build()
}

fun BufferView.readMapOfTraitNodes(
    lookup: FunctionLookup,
    loader: LoadIrModuleFn,
    tableView: BinaryTableView
): Pair<Int, Map<String, List<IrNode>>> {
    val paramsSize = readInt(0)
    val result = mapBuilder<String, List<IrNode>>()
    var position = 4
    repeat(paramsSize) {
        val key = tableView[readInt(position)]
        position += 4
        val listBuffer = bufferFrom(position)
        val (listPosition, listItems) = listBuffer.readListOfNodes(lookup, loader, tableView)
        position += listPosition
        result.put(key, listItems)
    }
    return position to result.build()
}

fun BufferView.readMapOfImplNodes(
    lookup: FunctionLookup,
    loader: LoadIrModuleFn,
    tableView: BinaryTableView
): Pair<Int, Map<Impl, List<IrNode>>> {
    val handle = preludeModule.typeSystem.handle
    val paramsSize = readInt(0)
    val result = mapBuilder<Impl, List<IrNode>>()
    var position = 4
    repeat(paramsSize) {
        val implBuffer = bufferFrom(position)
        val key = bufferFrom(position).readImpl(handle, tableView)
        position += implBuffer.readInt(0)
        val listBuffer = bufferFrom(position)
        val (listPosition, listItems) = listBuffer.readListOfNodes(lookup, loader, tableView)
        position += listPosition
        result.put(key, listItems)
    }
    return position to result.build()
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


fun BufferView.readIrModule(loader: LoadIrModuleFn): IrModule {
    val stringPoolSize = readInt(0)
    val offset = 4
    val stringPool = StringPoolView(bufferFrom(offset))
    val lookup = functionLookup()
    return bufferFrom(offset + stringPoolSize).readIrNode(lookup, loader, stringPool).cast<IrModule>().also {
        lookup.link(it)
    }
}
