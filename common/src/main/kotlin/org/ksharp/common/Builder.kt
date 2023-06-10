package org.ksharp.common

import org.ksharp.common.annotation.Mutable
import java.util.*

typealias ToValueAction<State, Value> = (state: State) -> Value

typealias ListBuilder<Item> = Builder<MutableList<Item>, List<Item>>
typealias MapBuilder<Key, Value> = Builder<MutableMap<Key, Value>, Map<Key, Value>>

interface MapView<K, V> {
    operator fun get(key: K): V?
    fun containsKey(key: K): Boolean?
}

class MapViewImpl<K, V> internal constructor(private val builder: MapBuilder<K, V>) {
    operator fun get(key: K) = builder.get(key)

    fun containsKey(key: K) = builder.containsKey(key)
}

@Mutable
class Builder<T, Value>(
    private var state: T,
    private val toValue: ToValueAction<T, Value>
) {
    private var built = false

    fun build(): Value {
        built = true
        return toValue(state)
    }

    fun mutate(block: (T) -> T): Boolean =
        if (!built) {
            state = block(state)
            true
        } else false

    fun <V> execute(block: (T) -> V): V? =
        if (!built) {
            block(state)
        } else null
}

@Mutable
fun <T, Value> builder(
    state: T,
    toValue: ToValueAction<T, Value>
) = Builder(state, toValue)

@Mutable
fun <Item> listBuilder(): ListBuilder<Item> = builder(
    mutableListOf()
) { Collections.unmodifiableList(it) }


fun <V> ListBuilder<V>.add(value: V) =
    this.mutate {
        it.add(value)
        it
    }

fun <V> ListBuilder<V>.addAll(value: List<V>) =
    this.mutate {
        it.addAll(value)
        it
    }


fun <V> ListBuilder<V>.size() =
    execute {
        it.size
    } ?: 0

@Mutable
fun <Key, Value> mapBuilder(): MapBuilder<Key, Value> = builder(
    mutableMapOf()
) { Collections.unmodifiableMap(it) }

fun <K, V> MapBuilder<K, V>.containsKey(key: K) = execute {
    it.containsKey(key)
}

fun <K, V> MapBuilder<K, V>.put(key: K, value: V) = this.mutate {
    it[key] = value
    it
}

fun <K, V> MapBuilder<K, V>.get(key: K) = execute {
    it[key]
}

val <K, V> MapBuilder<K, V>.view get() = MapViewImpl(this)
