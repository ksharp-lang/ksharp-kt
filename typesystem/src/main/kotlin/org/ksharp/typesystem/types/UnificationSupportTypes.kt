package org.ksharp.typesystem.types

import org.ksharp.common.HandlePromise
import org.ksharp.common.cast
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

data class ImplType internal constructor(
    val trait: TraitType,
    val impl: Type
) : Type {
    override val typeSystem: HandlePromise<TypeSystem>
        get() = impl.typeSystem
    override val serializer: TypeSerializer
        get() = TypeSerializers.ImplType
    override val unification: TypeUnification
        get() = TypeUnifications.Impl
    override val substitution: Substitution
        get() = Substitutions.NoDefined
    override val solver: Solver
        get() = Solvers.PassThrough
    override val terms: Sequence<Type>
        get() = impl.terms
    override val attributes: Set<Attribute>
        get() = impl.attributes

    override val representation: String
        get() = impl.representation

    override fun new(attributes: Set<Attribute>): Type {
        return ImplType(trait, impl.new(attributes))
    }

    override fun toString(): String = impl.toString()

}

data class FixedTraitType internal constructor(
    val trait: TraitType
) : Type {
    override val typeSystem: HandlePromise<TypeSystem>
        get() = trait.typeSystem
    override val serializer: TypeSerializer
        get() = TypeSerializers.FixedTraitType
    override val unification: TypeUnification
        get() = TypeUnifications.FixedTrait
    override val substitution: Substitution
        get() = Substitutions.NoDefined
    override val solver: Solver
        get() = Solvers.PassThrough
    override val terms: Sequence<Type>
        get() = trait.terms
    override val attributes: Set<Attribute>
        get() = trait.attributes

    override val representation: String
        get() = trait.representation

    override fun new(attributes: Set<Attribute>): Type =
        FixedTraitType(trait.new(attributes).cast())

    override fun toString(): String = trait.toString()

}
