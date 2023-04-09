package org.ksharp.module.bytecode

import io.netty.buffer.ByteBuf
import io.netty.buffer.PooledByteBufAllocator
import java.io.OutputStream
import java.nio.ByteBuffer

private val allocator = PooledByteBufAllocator.DEFAULT


/**
 * Calling methods after the buffer is written produce an exception
 */
class BufferWriter internal constructor() {
    private val buffer: ByteBuf  = allocator.directBuffer()
    fun add(value: String): Int = buffer.writeCharSequence(value, Charsets.UTF_8)

    fun set(index: Int, value: Int) {
        buffer.setInt(index, value)
    }
    fun add(value: Int) {
        buffer.writeInt(value)
    }
    fun writeTo(output: OutputStream): Int =
        buffer.readableBytes().apply{
            buffer.readBytes(output, this)
            buffer.release()
        }
}

class BufferView internal constructor (private val byteBuf: ByteBuf){
    fun readInt(index: Int) = byteBuf.getInt(index)

    fun readString(index: Int, size: Int): String = byteBuf.toString(index, size, Charsets.UTF_8)
}