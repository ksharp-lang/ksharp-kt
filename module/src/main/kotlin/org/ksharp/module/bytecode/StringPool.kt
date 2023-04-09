package org.ksharp.module.bytecode

import org.ksharp.common.get
import org.ksharp.common.io.BinaryTable
import org.ksharp.common.io.BufferView
import org.ksharp.common.io.newBufferWriter
import org.ksharp.common.mapBuilder
import org.ksharp.common.put
import java.io.OutputStream

typealias Position = Int
typealias StringPosition = Pair<Position, Position>

private const val StringPositionSize = 8
private const val Size = 4

/**
 *  Calling methods after the buffer is written produce an exception
 */
class StringPoolBuilder : BinaryTable {
    private var lastPosition: Position = -1
    private var lastSize: Int = 0
    private val dictionary = mapBuilder<String, Int>()
    private val indices = newBufferWriter().apply {
        add(0)
    }
    private val pool = newBufferWriter()
    val size: Int get() = indices.size + pool.size
    override fun add(name: String): Position =
        dictionary.get(name) ?: run {
            val size = pool.add(name)
            indices.apply {
                add(lastSize)
                add(size)
            }
            lastSize += size
            lastPosition += 1
            dictionary.put(name, lastPosition)
            lastPosition
        }

    fun writeTo(output: OutputStream) {
        indices.set(0, lastPosition + 1)
        indices.transferTo(output)
        pool.transferTo(output)
    }
}

class StringPoolView internal constructor(
    private val index: Int,
    private val buffer: BufferView
) {

    val size: Int = buffer.readInt(index)

    private val poolIndex: Int = size * StringPositionSize + Size

    operator fun get(index: Int): String {
        val nIndex = Size + (index * StringPositionSize)
        val start = buffer.readInt(this.index + nIndex)
        val size = buffer.readInt(this.index + nIndex + Size)
        return buffer.readString(this.poolIndex + start, size)
    }

}