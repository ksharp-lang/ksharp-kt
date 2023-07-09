package org.ksharp.parser.ksharp

import java.util.*

private data class Offset(var size: Int, var fixed: Boolean, var optional: Boolean)

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
            val isOptional = offsets.pop().optional
            return update(size, if (isOptional) sameResult else OffsetAction.PREVIOUS)
        }
        if (last.size < size) {
            if (last.fixed) return OffsetAction.INVALID
            last.size = size
            last.fixed = true
            last.optional = false
        }
        return sameResult
    }

    fun add(size: Int, optional: Boolean): Boolean {
        val allowed = if (offsets.isEmpty()) true else {
            offsets.peek().size < size
        }
        if (allowed) offsets.push(Offset(size, false, optional))
        return allowed
    }

    fun update(size: Int) = update(size, OffsetAction.SAME)
}
