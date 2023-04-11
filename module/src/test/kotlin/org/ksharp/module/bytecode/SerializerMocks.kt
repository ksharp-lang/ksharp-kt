package org.ksharp.module.bytecode

import org.ksharp.common.*
import org.ksharp.common.io.BinaryTable
import org.ksharp.common.io.BinaryTableView
import java.util.concurrent.atomic.AtomicInteger


fun mockStringTable(items: ListBuilder<String>) = object : BinaryTable {
    private val counter = AtomicInteger(-1)
    private val dictionary = mapBuilder<String, Int>()
    override fun add(name: String): Int =
        dictionary.get(name) ?: run {
            items.add(name)
            dictionary.put(name, counter.incrementAndGet())
            counter.get()
        }
}

fun mockStringTableView(items: List<String>) = object : BinaryTableView {
    override fun get(index: Int): String = items[index]
}