package org.ksharp.module.prelude

import org.ksharp.common.io.bufferView
import org.ksharp.module.ModuleInfo
import org.ksharp.module.bytecode.readModuleInfo

/**
 * Kernel module contains the minimal types and functions required to compile ks code
 */
private fun createKernelModule(): ModuleInfo = kernelTypeSystem
    .value
    .let { ts ->
        ModuleInfo(
            listOf(),
            typeSystem = ts,
            functions = mapOf(),
            traits = mapOf(),
            impls = setOf(),
        )
    }

val kernelModule = createKernelModule()

val preludeModule: ModuleInfo
    get() =
        String.Companion::class.java.getResourceAsStream("/org/ksharp/module/prelude.ksm")!!
            .use { input ->
                input.bufferView {
                    it.readModuleInfo()
                }
            }
