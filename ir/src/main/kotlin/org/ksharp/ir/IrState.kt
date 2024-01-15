package org.ksharp.ir

import org.ksharp.typesystem.attributes.Attribute

data class IrState(
    val moduleName: String,
    val functionLookup: FunctionLookup,
    val variableIndex: MutableVariableIndex,
) {
    fun addVariable(name: String, attribute: Set<Attribute>): VarInfo =
        VarInfo(variableIndex.size, VarKind.Var, attribute).also {
            variableIndex[name] = it
        }

}
