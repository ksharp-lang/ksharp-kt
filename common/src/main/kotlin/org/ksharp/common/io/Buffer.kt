package org.ksharp.common.io

import io.netty.buffer.ByteBuf
import io.netty.buffer.PooledByteBufAllocator
import java.io.OutputStream

private val allocator = PooledByteBufAllocator.DEFAULT


/**
 * Calling methods after the buffer is written produce an exception
 */
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