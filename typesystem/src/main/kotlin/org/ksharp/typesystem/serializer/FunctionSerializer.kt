package org.ksharp.typesystem.serializer

import org.ksharp.common.HandlePromise
import org.ksharp.common.io.*
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.readAttributes
import org.ksharp.typesystem.attributes.writeTo
import org.ksharp.typesystem.types.*

class FunctionSerializer : SerializerWriter<FullFunctionType>, TypeSerializerReader<FullFunctionType> {
    override fun write(input: FullFunctionType, buffer: BufferWriter, table: BinaryTable) {
        val scope = input.scope
        buffer.add(scope.type.ordinal)
        buffer.add(if (scope.trait == null) -1 else table.add(scope.trait))
        buffer.add(if (scope.impl == null) -1 else table.add(scope.impl))
        input.attributes.writeTo(buffer, table)
        input.arguments.writeTo(buffer, table)
    }

    override fun read(handle: HandlePromise<TypeSystem>, buffer: BufferView, table: BinaryTableView): FullFunctionType {
        val scopeType = FunctionScopeType.entries[buffer.readInt(0)]
        val trait = buffer.readInt(4).let {
            if (it == -1) null else table[it]
        }
        val impl = buffer.readInt(8).let {
            if (it == -1) null else table[it]
        }
        return FullFunctionType(
            handle,
            buffer.bufferFrom(12).readAttributes(table),
            buffer.bufferFrom(12 + buffer.readInt(12)).readListOfTypes(handle, table),
            FunctionScope(scopeType, trait, impl)
        )
    }
}

class PartialFunctionSerializer : SerializerWriter<PartialFunctionType>, TypeSerializerReader<PartialFunctionType> {
    override fun write(input: PartialFunctionType, buffer: BufferWriter, table: BinaryTable) {
        input.function.writeTo(buffer, table)
        input.arguments.writeTo(buffer, table)
    }

    override fun read(
        handle: HandlePromise<TypeSystem>,
        buffer: BufferView,
        table: BinaryTableView
    ): PartialFunctionType =
        PartialFunctionType(
            buffer.bufferFrom(buffer.readInt(0)).readListOfTypes(handle, table),
            buffer.readType<FunctionType>(handle, table)
        )
}
