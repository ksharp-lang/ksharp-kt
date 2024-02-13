package strings.impls

import org.ksharp.common.cast
import org.ksharp.ir.Call

class ComparableForStringCompare2 : Call {
    override fun execute(vararg arguments: Any): Any {
        val (left, right) = arguments
        val compareTo = left.cast<String>().compareTo(right.cast())
        return when {
            compareTo < 0 -> "Less"
            compareTo > 0 -> "Greater"
            else -> "Equal"
        }
    }
}
