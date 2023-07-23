package org.ksharp.typesystem.types

import org.ksharp.common.Either
import org.ksharp.common.ErrorOrValue
import org.ksharp.common.new
import org.ksharp.typesystem.TypeFactoryBuilder
import org.ksharp.typesystem.TypeItemBuilder
import org.ksharp.typesystem.TypeSystemBuilder
import org.ksharp.typesystem.TypeSystemErrorCode
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.serializer.TypeSerializer
import org.ksharp.typesystem.serializer.TypeSerializers
import org.ksharp.typesystem.solver.Solver
import org.ksharp.typesystem.solver.Solvers
import org.ksharp.typesystem.substitution.Substitution
import org.ksharp.typesystem.substitution.Substitutions
import org.ksharp.typesystem.unification.TypeUnification
import org.ksharp.typesystem.unification.TypeUnifications

interface Type {
    val serializer: TypeSerializer
    val unification: TypeUnification
    val substitution: Substitution
    val solver: Solver
    val compound: Boolean get() = true
    val terms: Sequence<Type>
    val attributes: Set<Attribute>
    val representation: String get() = toString().let { s -> if (compound) "($s)" else s }
    fun new(attributes: Set<Attribute>): Type
}

sealed interface TypeVariable : Type {
    override val compound: Boolean get() = false
    override val terms: Sequence<Type> get() = emptySequence()

}

data class Concrete internal constructor(
    override val attributes: Set<Attribute>,
    val name: String,
) : Type {
    override val solver: Solver
        get() = Solvers.PassThrough
    override val serializer: TypeSerializer
        get() = TypeSerializers.Concrete

    override val unification: TypeUnification
        get() = TypeUnifications.Default

    override val substitution: Substitution
        get() = Substitutions.Identity

    override val compound: Boolean get() = false
    override val terms: Sequence<Type> get() = emptySequence()

    override fun toString(): String = name

    override fun new(attributes: Set<Attribute>): Type =
        Concrete(attributes, name)

}

fun TypeItemBuilder.alias(name: String): ErrorOrValue<TypeVariable> =
    Either.Right(Alias(name)).also {
        validation {
            if (it(name) == null)
                TypeSystemErrorCode.TypeNotFound.new("type" to name)
            else null
        }
    }

fun TypeSystemBuilder.type(attributes: Set<Attribute>, name: String) =
    item(attributes, name) {
        Either.Right(Concrete(attributes, name))
    }

fun TypeSystemBuilder.type(
    attributes: Set<Attribute>,
    name: String,
    factory: TypeFactoryBuilder
) =
    item(attributes, name) {
        factory().map {
            if (it is Alias) {
                if (it.name == name) Concrete(attributes, it.name)
                else TypeAlias(attributes, it.name)
            } else it
        }
    }
