package strings

import org.ksharp.ir.ConstantNativeCall

class Uppercase1 : ConstantNativeCall() {

    override fun execute(vararg arguments: Any): Any =
        arguments.first().toString().uppercase()
}
