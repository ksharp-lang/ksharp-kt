package org.ksharp.ir.transform

import org.ksharp.common.cast
import org.ksharp.ir.IrList
import org.ksharp.ir.IrMap
import org.ksharp.ir.IrSet

val IrListFactory: CustomApplicationIrNode = {
    val (attributes, symbols) = arguments.toIrSymbols()
    IrList(
        attributes,
        symbols,
        location
    )
}

val IrSetFactory: CustomApplicationIrNode = {
    val (attributes, symbols) = arguments.toIrSymbols()
    IrSet(
        attributes,
        symbols,
        location
    )
}

val IrMapFactory: CustomApplicationIrNode = {
    val (attributes, symbols) = arguments.toIrSymbols()
    IrMap(
        attributes,
        symbols.cast(),
        location
    )
}
