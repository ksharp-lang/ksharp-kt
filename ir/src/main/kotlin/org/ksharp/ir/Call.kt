package org.ksharp.ir

import org.ksharp.typesystem.attributes.Attribute

fun interface Call {
    fun execute(vararg arguments: Any): Any

}

interface NativeCall : Call {
    fun getAttributes(attributes: Set<Attribute>): Set<Attribute>

}

abstract class ConstantNativeCall : NativeCall {
    override fun getAttributes(attributes: Set<Attribute>): Set<Attribute> =
        attributes.returnIfConstant

}

class FunctionCall(private val irFunction: IrFunction) : Call {
    override fun execute(vararg arguments: Any): Any =
        irFunction.call(*arguments)

}
