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

@Suppress("DataClassPrivateConstructor")
data class Alias private constructor(
    val name: String,
) : TypeVariable {

    override lateinit var typeSystem: HandlePromise<TypeSystem>
        private set

    internal constructor(typeSystem: HandlePromise<TypeSystem>, name: String) : this(name) {
        this.typeSystem = typeSystem
    }

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

@Suppress("DataClassPrivateConstructor")
data class TypeAlias private constructor(
    override val attributes: Set<Attribute>,
    val name: String
) : Type {

    override lateinit var typeSystem: HandlePromise<TypeSystem>
        private set

    internal constructor(typeSystem: HandlePromise<TypeSystem>, attributes: Set<Attribute>, name: String) : this(
        attributes,
        name
    ) {
        this.typeSystem = typeSystem
    }

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
