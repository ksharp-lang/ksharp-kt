package org.ksharp.compiler.loader

class CyclingReferences {
    private val pendingModules = mutableSetOf<String>()
    private val reference = mutableMapOf<String, MutableSet<String>>()
    private val referenced = mutableMapOf<String, MutableSet<String>>()

    val pending: Sequence<String>
        get() = pendingModules.asSequence()

    fun loading(module: String, from: String): Set<String> {
        if (from.isNotEmpty()) {
            (reference[from] ?: mutableSetOf<String>().also { reference[from] = it })
                .add(module)
            (referenced[module] ?: mutableSetOf<String>().also { referenced[module] = it })
                .add(from)
        }
        if (pendingModules.contains(module)) {
            return referenced[module] ?: emptySet()
        }
        pendingModules.add(module)
        return emptySet()
    }

    fun loaded(module: String) {
        pendingModules.remove(module)
        referenced[module]?.forEach { reference[it]?.remove(module) }
        referenced.remove(module)
    }

}
