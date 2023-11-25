package org.ksharp.compiler

import org.ksharp.module.ModuleInfo

fun interface ModuleLoader {

    /**
     * Loads module from [path] as a dependency for [from]
     */
    fun load(path: String, from: String): ModuleInfo

}
