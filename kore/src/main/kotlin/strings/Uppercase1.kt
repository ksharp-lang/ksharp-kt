package strings

import org.ksharp.ir.NativeCall
import org.ksharp.ir.returnIfConstant
import org.ksharp.typesystem.attributes.Attribute

class Uppercase1 : NativeCall {
    override fun getAttributes(attributes: Set<Attribute>): Set<Attribute> =
        attributes.returnIfConstant

    override fun execute(vararg arguments: Any): Any =
        arguments.first().toString().uppercase()
}
