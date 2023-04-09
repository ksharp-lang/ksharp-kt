package org.ksharp.common.io

interface BufferWriter {
    val size: Int
    fun add(value: String): Int
    fun set(index: Int, value: Int)
    fun add(value: Int)
}

interface BufferView {
    fun readInt(index: Int): Int

    fun readString(index: Int, size: Int): String
}