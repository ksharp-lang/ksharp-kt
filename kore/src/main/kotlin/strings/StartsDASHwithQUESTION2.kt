package strings

import org.ksharp.ir.ConstantNativeCall

class StartsDASHwithQUESTION2 : ConstantNativeCall() {

    override fun execute(vararg arguments: Any): Any =
        arguments.first().toString().startsWith(arguments.last().toString())
}
