package org.ksharp.compiler.loader

import org.ksharp.module.FunctionInfo
import org.ksharp.module.Impl
import org.ksharp.module.ModuleInfo
import org.ksharp.typesystem.TypeSystem

class ModuleInfoInterface(
    override val name: String,
    private val info: ModuleInfo,
    private val sources: SourceLoader
) : ModuleInterface {

    override val dependencies: Map<String, String> = info.dependencies
    override val typeSystem: TypeSystem = info.typeSystem
    override val functions: Map<String, FunctionInfo> = info.functions
    override val impls: Set<Impl> = info.impls
    override val executable: ModuleExecutable
        get() = TODO("Not yet implemented")

}
