package org.ksharp.ir

import org.ksharp.ir.transform.toIrSymbol
import org.ksharp.semantics.nodes.SemanticModuleInfo

data class Module(
    val dependencies: List<String>,
    val symbols: List<TopLevelSymbol>
) : IrNode

fun SemanticModuleInfo.toIrModule() =
    Module(
        listOf(),
        abstractions.map { it.toIrSymbol() }
    )
