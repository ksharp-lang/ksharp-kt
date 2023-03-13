package org.ksharp.common

class Flag {
    var enabled: Boolean = false
        private set

    fun activate() {
        enabled = true
    }
}