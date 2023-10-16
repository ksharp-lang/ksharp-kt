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

data class TypeConstructor(
    override val typeSystem: HandlePromise<TypeSystem>,
    override val attributes: Set<Attribute>,
    val name: String,
    val alias: String
) : Type {
    override val solver: Solver
        get() = Solvers.PassThrough
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

    override fun new(attributes: Set<Attribute>): Type = TypeConstructor(typeSystem, attributes, name, alias)

}
