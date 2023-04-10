package org.ksharp.common.io

interface BinaryTable {
    fun add(name: String): Int

}

interface BinaryTableView {
    operator fun get(index: Int): String
}