package org.ksharp.typesystem.types

import org.ksharp.common.*
import org.ksharp.typesystem.TypeItemBuilder
import org.ksharp.typesystem.serializer.TypeSerializer
import org.ksharp.typesystem.serializer.TypeSerializers
import org.ksharp.typesystem.substitution.Substitution
import org.ksharp.typesystem.substitution.Substitutions
import org.ksharp.typesystem.unification.TypeUnification
import org.ksharp.typesystem.unification.TypeUnifications
import org.ksharp.typesystem.validateTypeName

typealias UnionTypeFactoryBuilder = UnionTypeFactory.() -> Unit

data class UnionType internal constructor(
    override val visibility: TypeVisibility,
    val arguments: Map<String, ClassType>,
) : Type {
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

    data class ClassType internal constructor(
        override val visibility: TypeVisibility,
        val label: String,
        val params: List<Type>
    ) : Type {
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
    }
}


class UnionTypeFactory(
    private val unionType: String,
    private val factory: TypeItemBuilder
) {
    private var result: ErrorOrValue<MapBuilder<String, UnionType.ClassType>> = Either.Right(mapBuilder())

    fun clazz(label: String, parameters: ParametricTypeFactoryBuilder = {}) {
        result = result.flatMap { params ->
            validateTypeName(label).flatMap {
                factory.add(label, TypeConstructor(factory.visibility, label, unionType))
                ParametricTypeFactory(factory).apply(parameters).build().map { args ->
                    params.put(
                        label, UnionType.ClassType(
                            factory.visibility,
                            label = label,
                            params = args
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
    UnionTypeFactory(name, this).apply(factory).build().map {
        UnionType(visibility, it)
    }