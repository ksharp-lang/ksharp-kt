package org.ksharp.ir

data class Module(
    val dependencies: List<String>,
    val symbols: List<TopLevelSymbol>
) : IrNode
