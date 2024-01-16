package ir_test

import org.ksharp.common.cast
import org.ksharp.ir.NativeCall
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.CommonAttribute

class Fn1 : NativeCall {
    override fun getAttributes(attributes: Set<Attribute>): Set<Attribute> {
        if (attributes.contains(CommonAttribute.Constant)) {
            return attributes
        }
        return emptySet()
    }

    override fun execute(vararg arguments: Any): Any {
        return arguments.first().cast<String>().length
    }
}
