package org.ksharp.compiler.loader


import org.ksharp.ir.serializer.writeTo
import org.ksharp.ir.toIrModule
import org.ksharp.module.CodeModule
import org.ksharp.module.FunctionInfo
import org.ksharp.module.Impl
import org.ksharp.module.bytecode.writeTo
import org.ksharp.typesystem.TypeSystem

class CodeModuleInterface(
    private val module: CodeModule,
    private val sources: SourceLoader
) : ModuleInterface {

    override val name: String = module.name
    override val dependencies: Map<String, String> = module.module.dependencies
    override val typeSystem: TypeSystem = module.module.typeSystem
    override val functions: Map<String, FunctionInfo> = module.module.functions
    override val impls: Set<Impl> = module.module.impls

    fun compile() {
        sources.outputStream(name.toModulePath(".ksm")).let {
            module.module.writeTo(it)
        }
        sources.outputStream(name.toModulePath(".ksc")).let {
            module.toIrModule().writeTo(it)
        }
    }

    override val executable: ModuleExecutable
        get() = TODO("Not yet implemented")
}
