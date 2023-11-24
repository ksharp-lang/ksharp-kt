package org.ksharp.common.io

import io.netty.buffer.ByteBuf
import io.netty.buffer.PooledByteBufAllocator
import io.netty.buffer.Unpooled
import java.io.InputStream
import java.io.OutputStream

private val allocator = PooledByteBufAllocator.DEFAULT

/**
 * Endianess is BIG_ENDIAN
 */
interface BufferWriter {

    val size: Int
    fun add(value: String): Int
    fun set(index: Int, value: Int)
    fun add(value: Int)
    fun add(value: Long)
    fun add(double: Double)

    /**
     * Once the buffer is transferred it is destroyed
     */
    fun transferTo(output: OutputStream)

    /**
     * Once the buffer is transferred it is destroyed
     */
    fun transferTo(buffer: BufferWriter)
}

/**
 * Endianess is BIG_ENDIAN
 */
interface BufferView {
    val offset: Int get() = 0

    fun readInt(index: Int): Int

    fun readLong(index: Int): Long

    fun readDouble(index: Int): Double

    fun readString(index: Int, size: Int): String

    fun bufferFrom(offset: Int) = OffsetBufferView(offset, this)

}

class OffsetBufferView(override val offset: Int, private val bufferView: BufferView) : BufferView {
    override fun readLong(index: Int): Long = bufferView.readLong(offset + index)
    override fun readDouble(index: Int): Double = bufferView.readDouble(offset + index)
    override fun readInt(index: Int): Int = bufferView.readInt(offset + index)
    override fun readString(index: Int, size: Int): String = bufferView.readString(offset + index, size)
    override fun bufferFrom(offset: Int) = OffsetBufferView(offset + this.offset, bufferView)

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

    override fun add(value: Long) {
        buffer.writeLong(value)
    }

    override fun add(double: Double) {
        buffer.writeDouble(double)
    }

    override fun transferTo(output: OutputStream) {
        buffer.readBytes(output, buffer.readableBytes())
        buffer.release()
    }

    override fun transferTo(buffer: BufferWriter) {
        when (buffer) {
            is BufferWriterImpl -> buffer.buffer.writeBytes(this.buffer)
        }
        this.buffer.release()
    }

}

private class BufferViewImpl(private val byteBuf: ByteBuf) : BufferView {
    override fun readInt(index: Int) = byteBuf.getInt(index)
    override fun readLong(index: Int): Long = byteBuf.getLong(index)
    override fun readDouble(index: Int): Double = byteBuf.getDouble(index)
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
