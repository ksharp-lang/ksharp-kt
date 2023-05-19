package org.ksharp.typesystem.types

import org.ksharp.typesystem.serializer.TypeSerializer
import org.ksharp.typesystem.serializer.TypeSerializers
import org.ksharp.typesystem.substitution.Substitution
import org.ksharp.typesystem.substitution.Substitutions
import org.ksharp.typesystem.unification.TypeUnification
import org.ksharp.typesystem.unification.TypeUnifications

data class TypeConstructor(
    override val visibility: TypeVisibility,
    val name: String,
    val alias: String
) : Type {
    override val substitution: Substitution
        get() = Substitutions.TypeConstructor

    override val serializer: TypeSerializer
        get() = TypeSerializers.NoType

    override val terms: Sequence<Type>
        get() = emptySequence()

    override val unification: TypeUnification
        get() = TypeUnifications.TypeConstructor

    override val representation: String
        get() = name

    override val compound: Boolean
        get() = false
}