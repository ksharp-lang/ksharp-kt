package org.ksharp.module

import org.ksharp.typesystem.ErrorOrType

interface ModuleInterface {

    val impls: Set<Impl>

    fun type(name: String): ErrorOrType

    fun function(name: String): FunctionInfo?

}

fun interface ModuleLoader {

    /**
     * Loads module from [path] as a dependency for [from]
     */
    fun load(path: String, from: String): ModuleInterface

}
