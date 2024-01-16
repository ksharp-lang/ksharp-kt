package org.ksharp.ir

import org.ksharp.typesystem.attributes.Attribute

interface NativeCall {
    fun getAttributes(attributes: Set<Attribute>): Set<Attribute>
    fun execute(vararg arguments: Any): Any

}
