package org.ksharp.common.io

import io.netty.buffer.ByteBuf
import io.netty.buffer.PooledByteBufAllocator
import io.netty.buffer.Unpooled
import java.io.InputStream
import java.io.OutputStream

private val allocator = PooledByteBufAllocator.DEFAULT

interface BufferWriter {
    val size: Int
    fun add(value: String): Int
    fun set(index: Int, value: Int)
    fun add(value: Int)

    /**
     * Once the buffer is transferred it is destroyed
     */
    fun transferTo(output: OutputStream)
}

interface BufferView {

    fun readInt(index: Int): Int

    fun readString(index: Int, size: Int): String
}

/**
 * Calling methods after the buffer is written produce an exception
 */
private class BufferWriterImpl : BufferWriter {
    private val buffer: ByteBuf = allocator.directBuffer()

    override val size: Int get() = buffer.readableBytes()
    override fun add(value: String): Int = buffer.writeCharSequence(value, Charsets.UTF_8)

    override fun set(index: Int, value: Int) {
        buffer.setInt(index, value)
    }

    override fun add(value: Int) {
        buffer.writeInt(value)
    }

    override fun transferTo(output: OutputStream) {
        buffer.readBytes(output, buffer.readableBytes())
        buffer.release()
    }

}

private class BufferViewImpl(private val byteBuf: ByteBuf) : BufferView {
    override fun readInt(index: Int) = byteBuf.getInt(index)

    override fun readString(index: Int, size: Int): String = byteBuf.toString(index, size, Charsets.UTF_8)
}

fun newBufferWriter(): BufferWriter = BufferWriterImpl()

fun <T> InputStream.bufferView(action: (view: BufferView) -> T): T {
    val buffer = Unpooled.buffer().apply {
        val bytes = this@bufferView.readBytes()
        writeBytes(bytes)
    }
    val result = action(BufferViewImpl(buffer))
    buffer.release()
    return result
}