package org.ksharp.module.prelude

import org.ksharp.module.ModuleInfo
import org.ksharp.module.moduleFunctions
import org.ksharp.typesystem.types.newNamedParameter

private fun createPreludeModule(): ModuleInfo = preludeTypeSystem
    .value
    .let { ts ->
        val boolType = ts["Bool"].valueOrNull!!
        val parameter = newNamedParameter("_a_")
        ModuleInfo(
            listOf(),
            typeSystem = ts,
            functions = moduleFunctions {
                add(emptyList(), "if", boolType, parameter, parameter, parameter)
            }
        )
    }

val preludeModule = createPreludeModule()
