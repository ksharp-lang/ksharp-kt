package org.ksharp.ir.serializer

import org.ksharp.common.cast
import org.ksharp.common.io.BinaryTable
import org.ksharp.common.io.BinaryTableView
import org.ksharp.common.io.BufferView
import org.ksharp.common.io.BufferWriter
import org.ksharp.ir.IrModule

class IrModuleSerializer : IrNodeSerializer<IrModule> {
    override fun write(input: IrModule, buffer: BufferWriter, table: BinaryTable) {
        input.symbols.writeTo(buffer, table)
    }

    override fun read(buffer: BufferView, table: BinaryTableView): IrModule {
        return IrModule(buffer.readListOfNodes(table).cast())
    }
}
