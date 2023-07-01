package org.ksharp.ir.transform

import org.ksharp.common.cast
import org.ksharp.ir.IrList
import org.ksharp.ir.IrMap
import org.ksharp.ir.IrSet

val IrListFactory: CustomApplicationIrNode = { fLookup, variableIndex ->
    val (attributes, symbols) = arguments.toIrSymbols(fLookup, variableIndex)
    IrList(
        attributes,
        symbols,
        location
    )
}

val IrSetFactory: CustomApplicationIrNode = { fLookup, variableIndex ->
    val (attributes, symbols) = arguments.toIrSymbols(fLookup, variableIndex)
    IrSet(
        attributes,
        symbols,
        location
    )
}

val IrMapFactory: CustomApplicationIrNode = { fLookup, variableIndex ->
    val (attributes, symbols) = arguments.toIrSymbols(fLookup, variableIndex)
    IrMap(
        attributes,
        symbols.cast(),
        location
    )
}
