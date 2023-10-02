package org.ksharp.typesystem.types

import org.ksharp.common.*
import org.ksharp.typesystem.*
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.serializer.TypeSerializer
import org.ksharp.typesystem.serializer.TypeSerializers
import org.ksharp.typesystem.solver.Solver
import org.ksharp.typesystem.solver.Solvers
import org.ksharp.typesystem.substitution.Substitution
import org.ksharp.typesystem.substitution.Substitutions
import org.ksharp.typesystem.unification.TypeUnification
import org.ksharp.typesystem.unification.TypeUnifications

typealias TraitTypeFactoryBuilder = TraitTypeFactory.() -> Unit

interface IsTrait

data class TraitType internal constructor(
    override val attributes: Set<Attribute>,
    val name: String,
    val param: String,
    val methods: Map<String, MethodType>,
) : Type, IsTrait {
    override val solver: Solver
        get() = Solvers.NoDefined
    override val serializer: TypeSerializer
        get() = TypeSerializers.TraitType

    override val unification: TypeUnification
        get() = TypeUnifications.NoDefined

    override val substitution: Substitution
        get() = Substitutions.NoDefined

    data class MethodType internal constructor(
        override val attributes: Set<Attribute>,
        val name: String,
        val arguments: List<Type>,
    ) : Type {

        override val solver: Solver
            get() = Solvers.NoDefined
        override val serializer: TypeSerializer
            get() = TypeSerializers.MethodType

        override val unification: TypeUnification
            get() = TypeUnifications.NoDefined

        override val substitution: Substitution
            get() = Substitutions.NoDefined

        override val compound: Boolean
            get() = false
        override val terms: Sequence<Type>
            get() = arguments.asSequence()

        override fun toString(): String =
            "${
                name.indexOf('/').let { if (it == -1) name else name.substring(0, it) }
            } :: ${arguments.joinToString(" -> ") { it.representation }}"

        override fun new(attributes: Set<Attribute>): Type = MethodType(attributes, name, arguments)
    }

    override val compound: Boolean
        get() = false

    override val terms: Sequence<Type>
        get() = methods.values.asSequence()

    override fun toString(): String = """
        |trait $name $param =
        |    ${methods.values.joinToString("\n    ") { it.representation }}
    """.trimMargin("|")

    override fun new(attributes: Set<Attribute>): Type = TraitType(attributes, name, param, methods)

}

class TraitTypeFactory(
    private val traitName: String,
    private val factory: TypeItemBuilder
) {
    private var result: ErrorOrValue<MapBuilder<String, TraitType.MethodType>> = Either.Right(mapBuilder())

    fun method(name: String, arguments: ParametricTypeFactoryBuilder = {}) {
        result = result.flatMap { params ->
            validateFunctionName(name).flatMap {
                ParametricTypeFactory(factory).apply(arguments).build().flatMap traitMethod@{ args ->
                    val traitMethodName = "${name}/${args.size}"
                    if (params.containsKey(traitMethodName) == true) {
                        return@traitMethod Either.Left(
                            TypeSystemErrorCode.DuplicateTraitMethod.new(
                                "name" to traitMethodName,
                                "trait" to traitName
                            )
                        )
                    }
                    params.put(
                        traitMethodName, TraitType.MethodType(
                            factory.attributes,
                            name,
                            args
                        )
                    )
                    Either.Right(params)
                }
            }
        }
    }

    fun error(error: Error) {
        result = Either.Left(error)
    }

    internal fun build() = result.map { it.build() }
}

fun TypeSystemBuilder.trait(
    attributes: Set<Attribute>,
    name: String,
    paramName: String,
    factory: TraitTypeFactoryBuilder
) =
    item(attributes, name) {
        validateTypeParamName(paramName).flatMap {
            TraitTypeFactory(name, this).apply(factory).build().map {
                TraitType(attributes, name, paramName, it)
            }
        }
    }
