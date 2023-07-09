package org.ksharp.parser.ksharp

import org.ksharp.common.Flag
import java.util.*

private data class Offset(var size: Int, var fixed: Boolean, var type: OffsetType)

enum class OffsetAction {
    Invalid,
    Same,
    Repeating,
    Previous,
    End
}

enum class OffsetType(val action: OffsetAction) {
    Normal(OffsetAction.Previous),
    Optional(OffsetAction.Same),
    Repeating(OffsetAction.Repeating)
}


class IndentationOffset {

    private val offsets = Stack<Offset>()
    private val newLine = Flag()

    val currentType: OffsetType get() = if (offsets.isEmpty()) OffsetType.Normal else offsets.peek().type

    private fun update(size: Int, sameResult: OffsetAction): OffsetAction {
        if (offsets.isEmpty()) {
            newLine.activate()
            return OffsetAction.End
        }

        val last = offsets.peek()
        if (last.size > size) {
            return update(size, offsets.pop().type.action)
        }

        if (last.size < size) {
            if (last.fixed) return OffsetAction.Invalid
            last.size = size
            last.fixed = true
            if (last.type == OffsetType.Optional)
                last.type = OffsetType.Normal
        }

        newLine.activate()
        return if (last.type == OffsetType.Repeating) {
            OffsetAction.Repeating
        } else sameResult
    }

    fun add(size: Int, type: OffsetType): Boolean {
        val allowed = if (offsets.isEmpty()) true else {
            offsets.peek().size < size
        }
        if (allowed) offsets.push(Offset(size, false, type))
        return allowed
    }

    fun addRelative(size: Int, type: OffsetType) {
        val position = if (offsets.isEmpty()) 0 else {
            offsets.peek().size + size
        }
        offsets.push(Offset(position, false, type))
    }

    fun update(size: Int) = update(size, OffsetAction.Same)

}
