package org.ksharp.typesystem.types

import org.ksharp.typesystem.annotations.Annotation
import org.ksharp.typesystem.serializer.TypeSerializer
import org.ksharp.typesystem.serializer.TypeSerializers
import org.ksharp.typesystem.substitution.Substitution
import org.ksharp.typesystem.substitution.Substitutions
import org.ksharp.typesystem.unification.TypeUnification
import org.ksharp.typesystem.unification.TypeUnifications

data class Annotated internal constructor(
    internal val annotations: List<Annotation>,
    val type: Type
) : Type by type {
    override val serializer: TypeSerializer
        get() = TypeSerializers.NoDefined
    override val unification: TypeUnification
        get() = TypeUnifications.Default

    override val substitution: Substitution
        get() = Substitutions.NoDefined

    override val terms: Sequence<Type>
        get() = sequenceOf(type)

    override val representation: String
        get() = toString()

    override fun toString(): String = "${annotations.joinToString(" ") { it.toString() }} $type"
}

internal fun Type.annotated(annotations: List<Annotation>) = Annotated(annotations, this)

val Type.annotations: Iterable<Annotation>
    get() = when (this) {
        is Annotated -> this.annotations.asIterable()
        else -> emptyList<Annotation>().asIterable()
    }