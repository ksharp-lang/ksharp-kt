package org.ksharp.module.prelude.types

import org.ksharp.module.prelude.serializer.TypeSerializers
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.serializer.TypeSerializer
import org.ksharp.typesystem.substitution.Substitution
import org.ksharp.typesystem.substitution.Substitutions
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.unification.TypeUnification
import org.ksharp.typesystem.unification.TypeUnifications

class CharType : Type {

    override val attributes: Set<Attribute>
        get() = setOf(CommonAttribute.Public)

    override val serializer: TypeSerializer
        get() = TypeSerializers.CharType

    override val unification: TypeUnification
        get() = TypeUnifications.Default

    override val substitution: Substitution
        get() = Substitutions.Identity

    override val compound: Boolean = false
    override val terms: Sequence<Type> = emptySequence()
    override val representation: String = "char<Char>"

    override fun toString(): String = representation
}

val charType = CharType()
