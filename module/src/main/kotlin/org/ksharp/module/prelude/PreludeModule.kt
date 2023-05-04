package org.ksharp.module.prelude

import org.ksharp.module.ModuleInfo

private fun createPreludeModule(): ModuleInfo = preludeTypeSystem
    .value
    .let { ts ->
        val numType = ts["Num"].valueOrNull!!
        ModuleInfo(
            listOf(),
            typeSystem = ts,
            functions = mapOf()
        )
    }

val preludeModule = createPreludeModule()