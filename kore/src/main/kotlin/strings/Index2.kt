package strings

import org.ksharp.common.cast
import org.ksharp.ir.ConstantNativeCall

class Index2 : ConstantNativeCall() {
    override fun execute(vararg arguments: Any): Any =
        arguments.first().cast<String>()[arguments[1].cast()]

}
