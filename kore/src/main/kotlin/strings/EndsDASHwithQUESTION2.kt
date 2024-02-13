package strings

import org.ksharp.ir.ConstantNativeCall

class EndsDASHwithQUESTION2 : ConstantNativeCall() {
    override fun execute(vararg arguments: Any): Any =
        arguments.first().toString().endsWith(arguments.last().toString())
}
