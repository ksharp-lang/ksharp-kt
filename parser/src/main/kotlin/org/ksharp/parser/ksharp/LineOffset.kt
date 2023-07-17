package org.ksharp.parser.ksharp

import org.ksharp.common.tailArray

private const val LineArraySize = 30

class LineOffset {
    private val offsets = tailArray<Int>(LineArraySize)

    fun add(offset: Int) {
        if (offsets.size == 0) {
            offsets.append(offset)
            return
        }
        val lastOffset = offsets[offsets.size - 1]
        if (lastOffset < offset) {
            offsets.append(offset)
        }
    }

    fun size(offset: Int): Int {
        val size = offsets.size - 1
        if (size == -1) {
            return offset - 0
        }
        return offset - (size downTo (size - LineArraySize).coerceAtLeast(0))
            .asSequence()
            .map { offsets[it] }
            .first {
                it <= offset
            }
    }
}
