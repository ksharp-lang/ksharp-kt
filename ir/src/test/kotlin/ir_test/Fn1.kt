package ir_test

import org.ksharp.common.cast
import org.ksharp.ir.ConstantNativeCall

class Fn1 : ConstantNativeCall() {

    override fun execute(vararg arguments: Any): Any {
        return arguments.first().cast<String>().length
    }
}
