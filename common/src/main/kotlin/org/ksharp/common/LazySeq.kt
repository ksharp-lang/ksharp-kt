package org.ksharp.common

import org.ksharp.common.annotation.Mutable

typealias Item<T> = () -> T?


@Mutable
class LazySeq<T> {
    private val items = mutableListOf<Item<T>>()

    fun append(item: Item<T>) {
        items.add(item)
    }

    fun asSequence(): Sequence<T> =
        items.asSequence()
            .mapNotNull { it() }

}

@Mutable
fun <Value> lazySeqBuilder(actions: ActionsBuilder<LazySeq<Value>>.() -> Unit = {}) = builder(
    LazySeq<Value>(),
    { it.asSequence() }
) {
    mutator<Item<Value>>(BaseActions.Add) { state, item ->
        state.apply {
            append(item)
        }
    }
    actions()
}

fun <T> Builder<LazySeq<T>, Sequence<T>>.add(item: Item<T>) =
    this<Unit>(BaseActions.Add, item)