package strings

import org.ksharp.common.cast
import org.ksharp.ir.NativeCall
import org.ksharp.ir.returnIfConstant
import org.ksharp.typesystem.attributes.Attribute

class Index2 : NativeCall {
    override fun getAttributes(attributes: Set<Attribute>): Set<Attribute> =
        attributes.returnIfConstant

    override fun execute(vararg arguments: Any): Any =
        arguments.first().cast<String>()[arguments[1].cast()]

}
