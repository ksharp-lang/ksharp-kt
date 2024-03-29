package org.ksharp.typesystem.types

import org.ksharp.common.HandlePromise
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.serializer.TypeSerializer
import org.ksharp.typesystem.serializer.TypeSerializers
import org.ksharp.typesystem.solver.Solver
import org.ksharp.typesystem.solver.Solvers
import org.ksharp.typesystem.substitution.Substitution
import org.ksharp.typesystem.substitution.Substitutions
import org.ksharp.typesystem.unification.TypeUnification
import org.ksharp.typesystem.unification.TypeUnifications

data class Labeled internal constructor(
    val label: String,
    val type: Type
) : Type {
    override val typeSystem: HandlePromise<TypeSystem>
        get() = type.typeSystem
    override val solver: Solver
        get() = Solvers.NoDefined
    override val attributes: Set<Attribute>
        get() = type.attributes

    override val serializer: TypeSerializer
        get() = TypeSerializers.Labeled

    override val unification: TypeUnification
        get() = TypeUnifications.Default

    override val substitution: Substitution
        get() = Substitutions.Labeled

    override val terms: Sequence<Type>
        get() = sequenceOf(type)

    override val representation: String
        get() = toString()

    override fun toString(): String = "$label: ${type.representation}"

    override fun new(attributes: Set<Attribute>): Type = Labeled(label, type.new(attributes))
}

internal fun Type.labeled(label: String?) = label?.let { Labeled(it, this) } ?: this

val Type.label: String?
    get() = when (this) {
        is Labeled -> label
        else -> null
    }
