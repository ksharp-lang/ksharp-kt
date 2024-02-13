package strings

import org.ksharp.ir.ConstantNativeCall

class Trim1 : ConstantNativeCall() {

    override fun execute(vararg arguments: Any): Any =
        arguments.first().toString().trim()
}
