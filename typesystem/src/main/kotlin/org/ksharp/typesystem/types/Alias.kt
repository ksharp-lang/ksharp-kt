package org.ksharp.typesystem.types

import org.ksharp.common.HandlePromise
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.serializer.TypeSerializer
import org.ksharp.typesystem.serializer.TypeSerializers
import org.ksharp.typesystem.solver.Solver
import org.ksharp.typesystem.solver.Solvers
import org.ksharp.typesystem.substitution.Substitution
import org.ksharp.typesystem.substitution.Substitutions
import org.ksharp.typesystem.unification.TypeUnification
import org.ksharp.typesystem.unification.TypeUnifications

data class Alias internal constructor(
    override val typeSystem: HandlePromise<TypeSystem>,
    val name: String,
) : TypeVariable {

    override val attributes: Set<Attribute> = NoAttributes
    override val solver: Solver
        get() = Solvers.Alias

    override val serializer: TypeSerializer
        get() = TypeSerializers.Alias

    override val unification: TypeUnification
        get() = TypeUnifications.Alias

    override val substitution: Substitution
        get() = Substitutions.Alias

    override fun toString(): String {
        return name
    }

    override fun new(attributes: Set<Attribute>): Type = this
}

data class TypeAlias(
    override val typeSystem: HandlePromise<TypeSystem>,
    override val attributes: Set<Attribute>,
    val name: String
) : Type {

    override val solver: Solver
        get() = Solvers.Alias
    override val serializer: TypeSerializer
        get() = TypeSerializers.TypeAlias
    override val unification: TypeUnification
        get() = TypeUnifications.Alias

    override val substitution: Substitution
        get() = Substitutions.Alias

    override val compound: Boolean get() = false
    override val terms: Sequence<Type> get() = emptySequence()

    override fun toString(): String {
        return name
    }

    override fun new(attributes: Set<Attribute>): Type = TypeAlias(typeSystem, attributes, name)
}

fun TypeSystem.alias(name: String): ErrorOrType =
    this[name].map {
        Alias(handle, name)
    }
