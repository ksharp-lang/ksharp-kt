package org.ksharp.parser.ksharp

import java.util.*

private data class Offset(var size: Int, var fixed: Boolean)

enum class OffsetAction {
    INVALID,
    SAME,
    PREVIOUS,
    END
}

class IndentationOffset {

    private val offsets = Stack<Offset>()
    private fun update(size: Int, sameResult: OffsetAction): OffsetAction {
        if (offsets.isEmpty()) return OffsetAction.END
        val last = offsets.peek()
        if (last.size > size) {
            offsets.pop()
            return update(size, OffsetAction.PREVIOUS)
        }
        if (last.size < size) {
            if (last.fixed) return OffsetAction.INVALID
            last.size = size
            last.fixed = true
        }
        return sameResult
    }

    fun add(size: Int): Boolean {
        val allowed = if (offsets.isEmpty()) true else {
            offsets.peek().size < size
        }
        if (allowed) offsets.push(Offset(size, false))
        return allowed
    }

    fun update(size: Int) = update(size, OffsetAction.SAME)
}