package org.ksharp.module.bytecode

import org.ksharp.common.*
import java.io.OutputStream

typealias Position = Int
typealias StringPosition = Pair<Position, Position>

private const val StringPositionSize = 8
private const val Size = 4

/**
 *  Calling methods after the buffer is written produce an exception
 */
class StringPoolBuilder {
    private var lastPosition: Position = 0
    private var lastSize: Int = 0
    private val dictionary = mapBuilder<String, Int>()
    private val indices = BufferWriter().apply {
        add(0)
    }
    private val pool = BufferWriter()

    fun add(value: String): Position =
        dictionary.get(value) ?: run {
            val size = pool.add(value)
            indices.apply {
                add(lastSize)
                add(size)
            }
            lastSize += size
            lastPosition += 1
            dictionary.put(value, lastPosition)
            lastPosition
        }

    fun writeTo(output: OutputStream) {
        indices.set(0, lastPosition)
        indices.writeTo(output)
        pool.writeTo(output)
    }
}

class StringPoolView internal constructor(
    private val index: Int,
    private val buffer: BufferView
) {

    private val poolIndex: Int = buffer.readInt(index) * StringPositionSize + Size

    operator fun get(index: Int): String {
        val nIndex = Size + (index * StringPositionSize)
        val start =  buffer.readInt(this.index + nIndex)
        val size = buffer.readInt(this.index + nIndex + Size)
        return buffer.readString(this.poolIndex + start, size)
    }

}