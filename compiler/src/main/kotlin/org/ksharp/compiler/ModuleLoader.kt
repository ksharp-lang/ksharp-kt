package org.ksharp.compiler

import org.ksharp.module.ModuleInterface

fun interface ModuleLoader {

    /**
     * Loads module from [path] as a dependency for [from]
     */
    fun load(path: String, from: String): ModuleInterface

}
