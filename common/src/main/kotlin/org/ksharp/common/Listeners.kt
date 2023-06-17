package org.ksharp.common

import org.ksharp.common.annotation.Mutable

@Mutable
class Listeners<L> {
    private var listeners = mutableListOf<L>()

    fun add(listener: L) = listeners.add(listener)

    operator fun invoke(action: L.() -> Unit) {
        listeners.forEach {
            it.action()
        }
    }
}