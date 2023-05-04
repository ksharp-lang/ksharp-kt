package org.ksharp.common

class Cache<K, V> {
    private val cache = mutableMapOf<K, V>()
    fun get(key: K, create: () -> V): V =
        cache[key] ?: create().also { cache[key] = it }
}

fun <K, V> cacheOf() = Cache<K, V>()