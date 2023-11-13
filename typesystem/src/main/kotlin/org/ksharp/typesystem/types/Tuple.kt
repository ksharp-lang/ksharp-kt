package org.ksharp.typesystem.types

import org.ksharp.common.HandlePromise
import org.ksharp.typesystem.TypeItemBuilder
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
data class TupleType private constructor(
    override val attributes: Set<Attribute>,
    val elements: List<Type>,
) : Type {

    override lateinit var typeSystem: HandlePromise<TypeSystem>
        private set

    internal constructor(
        typeSystem: HandlePromise<TypeSystem>,
        attributes: Set<Attribute>,
        elements: List<Type>
    ) : this(
        attributes,
        elements
    ) {
        this.typeSystem = typeSystem
    }

    override val solver: Solver
        get() = Solvers.Tuple
    override val serializer: TypeSerializer
        get() = TypeSerializers.TupleType

    override val unification: TypeUnification
        get() = TypeUnifications.Tuple

    override val substitution: Substitution
        get() = Substitutions.Tuple

    override val terms: Sequence<Type>
        get() = elements.asSequence()

    override val compound: Boolean = true
    override fun toString(): String = elements.asSequence().map { it.representation }.joinToString(", ")

    override fun new(attributes: Set<Attribute>): Type = TupleType(typeSystem, attributes, elements)
}

fun TypeItemBuilder.tupleType(factory: ParametricTypeFactoryBuilder) =
    ParametricTypeFactory(this.createForSubtypes()).apply(factory).build().map {
        TupleType(handle, attributes, it)
    }

fun List<Type>.toTupleType(typeSystem: HandlePromise<TypeSystem>, attributes: Set<Attribute> = NoAttributes) =
    TupleType(typeSystem, attributes, this)

fun List<Type>.toTupleType(typeSystem: TypeSystem, attributes: Set<Attribute> = NoAttributes) =
    TupleType(typeSystem.handle, attributes, this)
