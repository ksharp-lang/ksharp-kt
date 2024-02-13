package strings

import org.ksharp.ir.ConstantNativeCall

class Lowercase1 : ConstantNativeCall() {

    override fun execute(vararg arguments: Any): Any =
        arguments.first().toString().lowercase()
}
