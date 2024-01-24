package org.ksharp.ir.serializer

import org.ksharp.common.cast
import org.ksharp.common.io.BinaryTable
import org.ksharp.common.io.BinaryTableView
import org.ksharp.common.io.BufferView
import org.ksharp.common.io.BufferWriter
import org.ksharp.ir.FunctionLookup
import org.ksharp.ir.IrModule

class IrModuleSerializer : IrNodeSerializer<IrModule> {
    override fun write(input: IrModule, buffer: BufferWriter, table: BinaryTable) {
        input.symbols.writeTo(buffer, table)
        input.traitSymbols.writeTo(buffer, table)
        input.implSymbols.writeTo(buffer, table)
    }

    override fun read(lookup: FunctionLookup, buffer: BufferView, table: BinaryTableView): IrModule {
        val (listOffset, functions) = buffer.readListOfNodes(lookup, table)
        val (traitOffset, traitSymbols) = buffer.bufferFrom(listOffset).readMapOfTraitNodes(lookup, table)
        val (_, implSymbols) = buffer.bufferFrom(listOffset + traitOffset).readMapOfImplNodes(lookup, table)
        return IrModule(functions.cast(), traitSymbols.cast(), implSymbols.cast())
    }
}
