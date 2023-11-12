package org.ksharp.typesystem.types

import org.ksharp.common.*
import org.ksharp.typesystem.*
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
import java.util.concurrent.atomic.AtomicInteger

private var parameterIdCounter = AtomicInteger(-1)

typealias ParametricTypeFactoryBuilder = ParametricTypeFactory.() -> Unit

@Suppress("DataClassPrivateConstructor")
data class Parameter private constructor(
    val name: String,
) : TypeVariable {

    override lateinit var typeSystem: HandlePromise<TypeSystem>
        private set

    internal constructor(typeSystem: HandlePromise<TypeSystem>, name: String) : this(name) {
        this.typeSystem = typeSystem
    }

    override val attributes: Set<Attribute>
        get() = NoAttributes
    override val solver: Solver
        get() = Solvers.PassThrough
    override val serializer: TypeSerializer
        get() = TypeSerializers.Parameter

    override val unification: TypeUnification
        get() = TypeUnifications.Parameter

    override val substitution: Substitution
        get() = Substitutions.Parameter

    val intermediate: Boolean get() = name.startsWith("@")
    override fun toString(): String = name

    override fun new(attributes: Set<Attribute>): Type = this
}

fun resetParameterCounterForTesting() = parameterIdCounter.set(-1)
fun newParameterForTesting(id: Int) = Parameter(MockHandlePromise(), "@${id}")

fun TypeSystem.newParameter() = Parameter(handle, "@${parameterIdCounter.incrementAndGet()}")

fun TypeSystem.newNamedParameter(name: String) = Parameter(handle, name)

@Suppress("DataClassPrivateConstructor")
data class ParametricType private constructor(
    override val attributes: Set<Attribute>,
    val type: Type,
    val params: List<Type>
) : Type {
    override lateinit var typeSystem: HandlePromise<TypeSystem>
        private set

    internal constructor(
        typeSystem: HandlePromise<TypeSystem>,
        attributes: Set<Attribute>,
        type: Type,
        params: List<Type>
    ) : this(
        attributes,
        type,
        params
    ) {
        this.typeSystem = typeSystem
    }

    override val solver: Solver
        get() = Solvers.Parametric
    override val serializer: TypeSerializer
        get() = TypeSerializers.ParametricType

    override val unification: TypeUnification
        get() = TypeUnifications.Parametric

    override val substitution: Substitution
        get() = Substitutions.Parametric

    override val terms: Sequence<Type>
        get() = sequenceOf(sequenceOf(type), params.asSequence()).flatten()

    override fun toString(): String = "${if (type is TraitType) type.name else type.toString()} ${
        params.asSequence().map { it.representation }.joinToString(" ")
    }"

    override fun new(attributes: Set<Attribute>): Type = ParametricType(typeSystem, attributes, type, params)
}

interface ParametricTypeParam : Type

class ParametricTypeFactory(
    private val builder: TypeItemBuilder
) {
    private var result: ErrorOrValue<ListBuilder<Type>> = Either.Right(listBuilder())

    fun add(type: ParametricTypeParam) {
        result = result.map { params ->
            params.add(type)
            params
        }
    }

    fun parameter(name: String, label: String? = null) {
        result = result.flatMap { params ->
            validateTypeParamName(name).map {
                params.add(Parameter(builder.handle, it).labeled(label))
                params
            }
        }
    }

    fun type(name: String, label: String? = null) {
        result = result.flatMap { params ->
            builder.alias(name).map {
                params.add(it.labeled(label))
                params
            }
        }
    }

    fun parametricType(name: String, label: String? = null, builder: ParametricTypeFactoryBuilder) {
        result = result.flatMap { params ->
            this.builder.parametricType(name, builder).map {
                params.add(it.labeled(label))
                params
            }
        }
    }

    fun functionType(label: String? = null, builder: ParametricTypeFactoryBuilder) {
        result = result.flatMap { params ->
            this.builder.functionType(builder).map {
                params.add(it.labeled(label))
                params
            }
        }
    }

    fun tupleType(label: String?, builder: ParametricTypeFactoryBuilder) {
        result = result.flatMap { params ->
            this.builder.tupleType(builder).map {
                params.add(it.labeled(label))
                params
            }
        }
    }

    fun error(error: Error) {
        result = Either.Left(error)
    }


    internal fun build(): ErrorOrValue<List<Type>> = result.map { it.build() }
}

private fun Type?.validateParametricType(
    handle: HandlePromise<TypeSystem>,
    attributes: Set<Attribute>,
    types: List<Type>
): Error? =
    when {
        this is ParametricType && params.size != types.size ->
            TypeSystemErrorCode.InvalidNumberOfParameters.new(
                "type" to this,
                "number" to params.size,
                "configuredType" to ParametricType(handle, attributes, type, types)
            )

        this is TraitType && 1 != types.size -> TypeSystemErrorCode.InvalidNumberOfParameters.new(
            "type" to this,
            "number" to 1,
            "configuredType" to ParametricType(
                handle,
                attributes,
                Concrete(handle, NoAttributes, name),
                types
            )
        )

        else -> null
    }

fun TypeItemBuilder.parametricType(
    name: String,
    factory: ParametricTypeFactoryBuilder
) =
    if (name == this.name) {
        ParametricTypeFactory(this.createForSubtypes()).apply(factory).build().flatMap {
            if (it.isEmpty()) {
                Either.Left(
                    TypeSystemErrorCode.ParametricTypeWithoutParameters.new("type" to name)
                )
            } else
                Either.Right(ParametricType(handle, attributes, Alias(handle, name), it))
        }
    } else
        alias(name).flatMap { pType ->
            validation {
                it(name).let { t ->
                    if (!(t is ParametricType || t is TraitType)) {
                        TypeSystemErrorCode.NoParametrizedType.new("type" to pType.representation)
                    } else null
                }
            }
            ParametricTypeFactory(this.createForSubtypes()).apply(factory).build().flatMap { types ->
                validation { it(name).validateParametricType(handle, attributes, types) }
                Either.Right(ParametricType(handle, attributes, pType, types))
            }
        }


fun TypeSystemBuilder.parametricType(
    attributes: Set<Attribute>,
    name: String,
    factory: ParametricTypeFactoryBuilder
) =
    item(attributes, name) {
        this.parametricType(name, factory)
    }

val Type.parameters: Sequence<Parameter>
    get() = when (this) {
        is ParametricType -> this.params.asSequence().map {
            it.parameters
        }.flatten()

        is Parameter -> sequenceOf(this)
        else -> this.terms.map { it.parameters }.flatten()
    }
