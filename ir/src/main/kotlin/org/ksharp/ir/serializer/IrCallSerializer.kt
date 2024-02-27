package org.ksharp.ir.serializer

import org.ksharp.common.add
import org.ksharp.common.cast
import org.ksharp.common.io.BinaryTable
import org.ksharp.common.io.BinaryTableView
import org.ksharp.common.io.BufferView
import org.ksharp.common.io.BufferWriter
import org.ksharp.common.listBuilder
import org.ksharp.ir.*
import org.ksharp.ir.types.Symbol
import org.ksharp.module.prelude.preludeModule
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo
import org.ksharp.typesystem.serializer.readType
import org.ksharp.typesystem.serializer.writeTo
import org.ksharp.typesystem.types.Type


private fun CallScope.writeTo(buffer: BufferWriter, table: BinaryTable) {
    buffer.add(table.add(this.callName))
    this.traitName
        .let { if (it == null) -1 else table.add(it) }
        .let { buffer.add(it) }
    this.traitScopeName
        .let { if (it == null) -1 else table.add(it) }
        .let { buffer.add(it) }
}

private fun BufferView.readCallScope(table: BinaryTableView): Pair<Int, CallScope> {
    val callName = table[readInt(0)]
    val traitName = readInt(4).let { if (it == -1) null else table[it] }
    val traitScopeName = readInt(8).let { if (it == -1) null else table[it] }
    return 12 to CallScope(callName, traitName, traitScopeName)
}

class IrCallSerializer : IrNodeSerializer<IrCall> {
    override fun write(input: IrCall, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        input.module
            .let { if (it == null) -1 else table.add(it) }
            .let { buffer.add(it) }
        input.scope.writeTo(buffer, table)
        input.location.writeTo(buffer)
        input.type.writeTo(buffer, table)
        input.arguments.writeTo(buffer, table)
    }

    override fun read(
        lookup: FunctionLookup,
        loader: LoadIrModuleFn,
        buffer: BufferView,
        table: BinaryTableView
    ): IrCall {
        val attributes = buffer.readAttributes(table)
        var offset = buffer.readInt(0)
        val module = buffer.readInt(offset).let { if (it == -1) null else table[it] }
        offset += 4
        val (callScopePosition, callScope) = buffer.bufferFrom(offset).readCallScope(table)
        offset += callScopePosition
        val location = buffer.bufferFrom(offset).readLocation()
        offset += 16
        val type = buffer.bufferFrom(offset).readType<Type>(preludeModule.typeSystem.handle, table)
        offset += buffer.readInt(offset)
        val arguments = buffer.bufferFrom(offset).readListOfNodes(lookup, loader, table).second
        return IrCall(
            attributes,
            module,
            callScope,
            arguments.cast(),
            type,
            location
        )
    }
}

class IrNativeCallSerializer : IrNodeSerializer<IrNativeCall> {
    override fun write(input: IrNativeCall, buffer: BufferWriter, table: BinaryTable) {
        input.mAttributes.writeTo(buffer, table)
        buffer.add(table.add(input.functionClass))
        input.location.writeTo(buffer)
        input.type.writeTo(buffer, table)
        input.arguments.writeTo(buffer, table)
    }

    override fun read(
        lookup: FunctionLookup,
        loader: LoadIrModuleFn,
        buffer: BufferView,
        table: BinaryTableView
    ): IrNativeCall {
        val argAttributes = buffer.readAttributes(table)
        var offset = buffer.readInt(0)
        val functionClass = table[buffer.readInt(offset)]
        offset += 4
        val location = buffer.bufferFrom(offset).readLocation()
        offset += 16
        val type = buffer.bufferFrom(offset).readType<Type>(preludeModule.typeSystem.handle, table)
        offset += buffer.readInt(offset)
        val arguments = buffer.bufferFrom(offset).readListOfNodes(lookup, loader, table).second
        return IrNativeCall(
            argAttributes,
            functionClass,
            arguments.cast(),
            type,
            location
        )
    }
}

class IrModuleCallSerializer : IrNodeSerializer<IrModuleCall> {
    override fun write(input: IrModuleCall, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        buffer.add(table.add(input.moduleName))
        buffer.add(table.add(input.functionName))
        input.location.writeTo(buffer)
        input.type.writeTo(buffer, table)
        input.arguments.writeTo(buffer, table)
    }

    override fun read(
        lookup: FunctionLookup,
        loader: LoadIrModuleFn,
        buffer: BufferView,
        table: BinaryTableView
    ): IrModuleCall {
        val attributes = buffer.readAttributes(table)
        var offset = buffer.readInt(0)
        val moduleName = table[buffer.readInt(offset)]
        offset += 4
        val name = table[buffer.readInt(offset)]
        offset += 4
        val location = buffer.bufferFrom(offset).readLocation()
        offset += 16
        val type = buffer.bufferFrom(offset).readType<Type>(preludeModule.typeSystem.handle, table)
        offset += buffer.readInt(offset)
        val arguments = buffer.bufferFrom(offset).readListOfNodes(lookup, loader, table).second
        return IrModuleCall(
            attributes,
            loader,
            moduleName,
            name,
            arguments.cast(),
            type.cast(),
            location
        )
    }
}

class IrLambdaCallSerializer : IrNodeSerializer<IrLambdaCall> {
    override fun write(input: IrLambdaCall, buffer: BufferWriter, table: BinaryTable) {
        input.attributes.writeTo(buffer, table)
        input.lambda.serialize(buffer, table)
        input.location.writeTo(buffer)
        input.type.writeTo(buffer, table)
        input.arguments.writeTo(buffer, table)
    }

    override fun read(
        lookup: FunctionLookup,
        loader: LoadIrModuleFn,
        buffer: BufferView,
        table: BinaryTableView
    ): IrLambdaCall {
        val attributes = buffer.readAttributes(table)
        var offset = buffer.readInt(0)
        val lambda = buffer.bufferFrom(offset).readIrNode(lookup, loader, table).cast<IrExpression>()
        offset += buffer.readInt(offset)
        val location = buffer.bufferFrom(offset).readLocation()
        offset += 16
        val type = buffer.bufferFrom(offset).readType<Type>(preludeModule.typeSystem.handle, table)
        offset += buffer.readInt(offset)
        val arguments = buffer.bufferFrom(offset).readListOfNodes(lookup, loader, table).second
        return IrLambdaCall(
            attributes,
            lambda,
            arguments.cast(),
            type.cast(),
            location
        )
    }

}

class IrComparableSerializer : IrNodeSerializer<IrComparable> {
    override fun write(input: IrComparable, buffer: BufferWriter, table: BinaryTable) {
        input.expected.let {
            buffer.add(it.size)
            it.forEach { e -> buffer.add(table.add(e.value)) }
        }
        input.call.serialize(buffer, table)
    }

    override fun read(
        lookup: FunctionLookup,
        loader: LoadIrModuleFn,
        buffer: BufferView,
        table: BinaryTableView
    ): IrComparable {
        val size = buffer.readInt(0) + 1
        val expected = listBuilder<Symbol>()
        (1 until size).map {
            expected.add(Symbol(table[buffer.readInt(it * 4)]))
        }
        val call = buffer.bufferFrom(4 * size).readIrNode(lookup, loader, table).cast<IrModuleCall>()
        return IrComparable(call, expected.build())
    }
}
