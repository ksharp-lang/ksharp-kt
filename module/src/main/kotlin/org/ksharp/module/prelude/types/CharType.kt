package org.ksharp.module.prelude.types

import org.ksharp.typesystem.types.Type

class CharType : Type {
    override val compound: Boolean = false
    override val terms: Sequence<Type> = emptySequence()
    override val representation: String = "char<Char>"
}

val charType = CharType()