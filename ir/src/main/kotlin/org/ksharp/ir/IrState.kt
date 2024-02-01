package org.ksharp.ir

import org.ksharp.module.ModuleInfo
import org.ksharp.typesystem.attributes.Attribute

open class PartialIrState(
    open val moduleName: String,
    open val module: ModuleInfo,
    open val loader: LoadIrModuleFn,
    open val functionLookup: FunctionLookup,
) {
    fun toIrState(variableIndex: MutableVariableIndex): IrState =
        IrState(moduleName, module, loader, functionLookup, variableIndex)
}

class IrState(
    moduleName: String,
    module: ModuleInfo,
    loader: LoadIrModuleFn,
    functionLookup: FunctionLookup,
    val variableIndex: MutableVariableIndex,
) : PartialIrState(moduleName, module, loader, functionLookup) {
    fun addVariable(name: String, attribute: Set<Attribute>): VarInfo =
        VarInfo(variableIndex.size, VarKind.Var, attribute).also {
            variableIndex[name] = it
        }

}
