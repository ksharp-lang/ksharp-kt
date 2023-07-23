package org.ksharp.typesystem.types

import org.ksharp.common.*
import org.ksharp.typesystem.TypeItemBuilder
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
import org.ksharp.typesystem.validateTypeName

typealias UnionTypeFactoryBuilder = UnionTypeFactory.() -> Unit

data class UnionType internal constructor(
    override val attributes: Set<Attribute>,
    val arguments: Map<String, ClassType>,
) : Type {
    override val solver: Solver
        get() = Solvers.NoDefined
    override val serializer: TypeSerializer
        get() = TypeSerializers.UnionType

    override val unification: TypeUnification
        get() = TypeUnifications.Union

    override val substitution: Substitution
        get() = Substitutions.Union

    override val terms: Sequence<Type>
        get() = arguments.values.asSequence()

    override val compound: Boolean = false
    override fun toString(): String =
        arguments.asSequence().map { (_, value) -> value.representation }
            .joinToString("\n|")

    override fun new(attributes: Set<Attribute>): Type = UnionType(attributes, arguments)

    data class ClassType internal constructor(
        val label: String,
        val params: List<Type>
    ) : Type {
        override val attributes: Set<Attribute>
            get() = NoAttributes

        override val solver: Solver
            get() = Solvers.NoDefined
        override val serializer: TypeSerializer
            get() = TypeSerializers.ClassType

        override val unification: TypeUnification
            get() = TypeUnifications.NoDefined

        override val substitution: Substitution
            get() = Substitutions.ClassType

        override val terms: Sequence<Type>
            get() = params.asSequence()

        override val compound: Boolean = false
        override fun toString(): String = "$label${if (params.isEmpty()) "" else " "}${
            params.asSequence().map { it.representation }.joinToString(" ")
        }"

        override fun new(attributes: Set<Attribute>): Type = this
    }
}


class UnionTypeFactory(
    private val unionType: String,
    private val attributes: Set<Attribute>,
    private val factory: TypeItemBuilder
) {
    private var result: ErrorOrValue<MapBuilder<String, UnionType.ClassType>> = Either.Right(mapBuilder())

    fun clazz(label: String, parameters: ParametricTypeFactoryBuilder = {}) {
        result = result.flatMap { params ->
            validateTypeName(label).flatMap {
                factory.add(label, TypeConstructor(attributes, label, unionType))
                ParametricTypeFactory(factory.createForSubtypes()).apply(parameters).build().map { args ->
                    params.put(
                        label, UnionType.ClassType(
                            label,
                            args
                        )
                    )
                    params
                }
            }
        }
    }

    fun error(error: Error) {
        result = Either.Left(error)
    }

    internal fun build() = result.map { it.build() }
}

fun TypeItemBuilder.unionType(factory: UnionTypeFactoryBuilder) =
    UnionTypeFactory(name, attributes, this).apply(factory).build().map {
        UnionType(attributes, it)
    }
