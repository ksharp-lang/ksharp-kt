package strings

import org.ksharp.ir.ConstantNativeCall

class Length1 : ConstantNativeCall() {
    override fun execute(vararg arguments: Any): Any =
        arguments.first().toString().length

}
