package org.ksharp.module.prelude

import org.ksharp.common.io.bufferView
import org.ksharp.module.Impl
import org.ksharp.module.ModuleInfo
import org.ksharp.module.bytecode.readModuleInfo
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.attributes.nameAttribute

private fun TypeSystem.createNumImpl(type: String) =
    Impl(
        setOf(
            CommonAttribute.Native,
            nameAttribute(mapOf("ir" to "prelude::num"))
        ), "Num", this[type].valueOrNull!!
    )

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
            impls = setOf(
                ts.createNumImpl("Byte"),
                ts.createNumImpl("Short"),
                ts.createNumImpl("Int"),
                ts.createNumImpl("Long"),
                ts.createNumImpl("BigInt"),
                ts.createNumImpl("Float"),
                ts.createNumImpl("Double"),
                ts.createNumImpl("BigDecimal"),
            ),
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
