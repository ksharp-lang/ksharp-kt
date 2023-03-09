package org.ksharp.semantics.expressions

import org.ksharp.common.Error

data class ModuleFunctionInfo(
    val errors: List<Error>,
    val functionTable: FunctionTable
)