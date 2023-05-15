package org.ksharp.common.io

fun interface BinaryTable {
    fun add(name: String): Int

}

fun interface BinaryTableView {
    operator fun get(index: Int): String
}