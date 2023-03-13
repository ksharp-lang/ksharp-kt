package org.ksharp.module.prelude

import org.ksharp.module.ModuleInfo
import org.ksharp.module.moduleFunctions

private fun createPreludeModule(): ModuleInfo = preludeTypeSystem
    .value
    .let { ts ->
        val numType = ts["Num"].valueOrNull!!
        ModuleInfo(
            typeSystem = ts,
            functions = moduleFunctions {
                add("(+)", numType, numType, numType)
            }
        )
    }

val preludeModule = createPreludeModule()