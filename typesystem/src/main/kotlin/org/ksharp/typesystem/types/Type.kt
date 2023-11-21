package org.ksharp.typesystem.types

import org.ksharp.common.Either
import org.ksharp.common.ErrorOrValue
import org.ksharp.common.HandlePromise
import org.ksharp.common.new
import org.ksharp.typesystem.*
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.merge
import org.ksharp.typesystem.serializer.TypeSerializer
import org.ksharp.typesystem.serializer.TypeSerializers
import org.ksharp.typesystem.solver.Solver
import org.ksharp.typesystem.solver.Solvers
import org.ksharp.typesystem.solver.solve
import org.ksharp.typesystem.substitution.Substitution
import org.ksharp.typesystem.substitution.Substitutions
import org.ksharp.typesystem.unification.TypeUnification
import org.ksharp.typesystem.unification.TypeUnifications

private fun TypeSystem.resolve(type: Type): ErrorOrType =
    when (type) {
        is Alias -> this[type.name].flatMap {
            it()
        }

        is Labeled -> type.type().map {
            Labeled(type.label, it)
        }

        is TypeAlias -> this[type.name].flatMap {
            it()
        }.map { it.new(it.attributes.merge(type.attributes)) }

        is TypeConstructor -> this[type.alias]
        else -> Either.Right(type)
    }

interface Type {
    val typeSystem: HandlePromise<TypeSystem>

    val serializer: TypeSerializer
    val unification: TypeUnification
    val substitution: Substitution
    val solver: Solver

    val compound: Boolean get() = true
    val terms: Sequence<Type>
    val attributes: Set<Attribute>

    val representation: String get() = toString().let { s -> if (compound) "($s)" else s }

    fun new(attributes: Set<Attribute>): Type

    operator fun invoke(): ErrorOrType {
        val handle = typeSystem.handle
        return handle?.resolve(this) ?: throw IllegalStateException("TypeSystem not initialized")
    }
}

sealed interface TypeVariable : Type {
    override val compound: Boolean get() = false
    override val terms: Sequence<Type> get() = emptySequence()

}

@Suppress("DataClassPrivateConstructor")
data class Concrete private constructor(
    override val attributes: Set<Attribute>,
    val name: String,
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
        Concrete(typeSystem, attributes, name)

}

fun TypeItemBuilder.alias(name: String): ErrorOrValue<TypeVariable> =
    Either.Right(Alias(handle, name)).also {
        validation {
            if (it(name) == null)
                TypeSystemErrorCode.TypeNotFound.new("type" to name)
            else null
        }
    }

fun TypeSystemBuilder.type(attributes: Set<Attribute>, name: String) =
    item(attributes, name) {
        Either.Right(Concrete(this@type.handle, attributes, name))
    }

fun TypeSystemBuilder.type(
    attributes: Set<Attribute>,
    name: String,
    factory: TypeFactoryBuilder
) =
    item(attributes, name) {
        factory().map {
            if (it is Alias) {
                if (it.name == name) Concrete(it.typeSystem, attributes, it.name)
                else TypeAlias(it.typeSystem, attributes, it.name)
            } else it
        }
    }

val List<Type>.arity: Int
    get() =
        when (val s = this.size) {
            2 -> if (first().representation == "Unit") 0 else 1
            else -> s - 1
        }

val Type.isUnitType: Boolean
    get() = when (this) {
        is Concrete, is Alias, is TypeAlias, is Labeled -> this.solve().valueOrNull!!.representation == "Unit"
        else -> false
    }
