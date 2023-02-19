package org.ksharp.common

import org.ksharp.common.annotation.Mutable

typealias Item<T> = () -> T?

@Mutable
class LazySeq<T> {
    private val items = listBuilder<Item<T>>()

    fun append(item: Item<T>) {
        items.add(item)
    }

    fun asSequence(): Sequence<T> =
        items.build()
            .asSequence()
            .mapNotNull { it() }
}

@Mutable
fun <Value> lazySeqBuilder() = builder(
    LazySeq<Value>()
) { it.asSequence() }

fun <T> Builder<LazySeq<T>, Sequence<T>>.add(item: Item<T>) = this.mutate {
    it.append(item)
    it
}