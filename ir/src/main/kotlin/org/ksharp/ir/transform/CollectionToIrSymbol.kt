package org.ksharp.ir.transform

import org.ksharp.common.cast
import org.ksharp.ir.IrArray
import org.ksharp.ir.IrList
import org.ksharp.ir.IrMap
import org.ksharp.ir.IrSet

val IrArrayFactory: CustomApplicationIrNode = { state ->
    val (attributes, symbols) = arguments.toIrSymbols(state)
    IrArray(
        attributes,
        symbols,
        location
    )
}


val IrListFactory: CustomApplicationIrNode = { state ->
    val (attributes, symbols) = arguments.toIrSymbols(state)
    IrList(
        attributes,
        symbols,
        location
    )
}

val IrSetFactory: CustomApplicationIrNode = { state ->
    val (attributes, symbols) = arguments.toIrSymbols(state)
    IrSet(
        attributes,
        symbols,
        location
    )
}

val IrMapFactory: CustomApplicationIrNode = { state ->
    val (attributes, symbols) = arguments.toIrSymbols(state)
    IrMap(
        attributes,
        symbols.cast(),
        location
    )
}
