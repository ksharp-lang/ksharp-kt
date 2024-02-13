package strings

import org.ksharp.ir.NativeCall
import org.ksharp.ir.returnIfConstant
import org.ksharp.typesystem.attributes.Attribute

class Starts_dash_with2 : NativeCall {
    override fun getAttributes(attributes: Set<Attribute>): Set<Attribute> =
        attributes.returnIfConstant

    override fun execute(vararg arguments: Any): Any =
        arguments.first().toString().startsWith(arguments.last().toString())
}
