package org.ksharp.compiler.loader

import org.ksharp.common.cast
import org.ksharp.ir.IrFunction
import org.ksharp.ir.IrModule

class IrModuleExecutable(
    private val irModule: IrModule
) : ModuleExecutable {
    override fun execute(name: String, vararg args: Any): Any =
        irModule.symbols.first {
            it.name == name && it is IrFunction
        }.cast<IrFunction>()
            .call(*args)

}
