package org.ksharp.common

typealias TableValue<V> = Pair<V, Location>

fun interface Table<Value> {
    operator fun get(name: String): TableValue<Value>?
}

interface TableBuilder<Value> {

    operator fun get(name: String): TableValue<Value>?

    fun register(
        name: String,
        value: Value,
        location: Location
    ): ErrorOrValue<Boolean>

    fun build(): Table<Value>

}
