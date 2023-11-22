package org.ksharp.ir

import org.ksharp.ir.serializer.IrNodeSerializers

interface IrNode {
    val serializer: IrNodeSerializers
}
