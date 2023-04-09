package org.ksharp.module.bytecode

import io.netty.buffer.ByteBuf
import io.netty.buffer.PooledByteBufAllocator
import org.ksharp.common.io.BufferView
import org.ksharp.common.io.BufferWriter
import java.io.OutputStream

private val allocator = PooledByteBufAllocator.DEFAULT


/**
 * Calling methods after the buffer is written produce an exception
 */
class BufferWriterImpl: BufferWriter {
    private val buffer: ByteBuf  = allocator.directBuffer()

    override val size: Int get() = buffer.readableBytes()
    override fun add(value: String): Int = buffer.writeCharSequence(value, Charsets.UTF_8)

    override fun set(index: Int, value: Int) {
        buffer.setInt(index, value)
    }
    override fun add(value: Int) {
        buffer.writeInt(value)
    }
    fun writeTo(output: OutputStream): Int =
        buffer.readableBytes().apply{
            buffer.readBytes(output, this)
            buffer.release()
        }


}

class BufferViewImpl(private val byteBuf: ByteBuf): BufferView {
    override fun readInt(index: Int) = byteBuf.getInt(index)

    override fun readString(index: Int, size: Int): String = byteBuf.toString(index, size, Charsets.UTF_8)
}