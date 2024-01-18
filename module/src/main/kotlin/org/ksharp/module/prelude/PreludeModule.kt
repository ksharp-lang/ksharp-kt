package org.ksharp.module.prelude

import org.ksharp.common.io.bufferView
import org.ksharp.module.Impl
import org.ksharp.module.ModuleInfo
import org.ksharp.module.bytecode.readModuleInfo
import org.ksharp.module.prelude.types.Numeric
import org.ksharp.typesystem.TypeSystem

private fun TypeSystem.preludeImpls(): Set<Impl> =
    sequenceOf(Numeric.entries
        .asSequence()
        .map {
            sequenceOf(
                Impl("Num", this[it.name].valueOrNull!!),
                Impl("Comparable", this[it.name].valueOrNull!!)
            )
        }
        .flatten(),
        Numeric.entries
            .asSequence()
            .filter { it.isInteger }
            .map { Impl("Bitwise", this[it.name].valueOrNull!!) })
        .flatten()
        .toSet()

/**
 * Kernel module contains the minimal types and functions required to compile ks code
 */
private fun createKernelModule(): ModuleInfo = kernelTypeSystem
    .value
    .let { ts ->
        ModuleInfo(
            mapOf(),
            typeSystem = ts,
            functions = mapOf(),
            impls = ts.preludeImpls()
        )
    }

val kernelModule = createKernelModule()

val preludeModule: ModuleInfo
    get() =
        String.Companion::class.java.getResourceAsStream("/org/ksharp/module/prelude.ksm")!!
            .use { input ->
                input.bufferView {
                    val kernelTypeSystem = kernelTypeSystem.value
                    it.readModuleInfo(kernelTypeSystem).let { module ->
                        module.copy(impls = module.impls + kernelTypeSystem.preludeImpls())
                    }
                }
            }
