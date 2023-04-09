package org.ksharp.module.prelude.types

import org.ksharp.typesystem.serializer.TypeSerializer
import org.ksharp.typesystem.serializer.TypeSerializers
import org.ksharp.typesystem.types.Type

class CharType : Type {
    override val serializer: TypeSerializer
        get() = TypeSerializers.NoDefined
    
    override val compound: Boolean = false
    override val terms: Sequence<Type> = emptySequence()
    override val representation: String = "char<Char>"
}

val charType = CharType()