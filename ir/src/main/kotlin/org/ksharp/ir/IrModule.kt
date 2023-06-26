package org.ksharp.ir

import org.ksharp.ir.transform.toIrSymbol
import org.ksharp.semantics.nodes.SemanticModuleInfo

data class IrModule(
    val dependencies: List<String>,
    val symbols: List<IrTopLevelSymbol>
) : IrNode

fun SemanticModuleInfo.toIrModule() =
    IrModule(
        listOf(),
        abstractions.map { it.toIrSymbol(emptyVariableIndex) }
    )
