package org.ksharp.typesystem.types

import org.ksharp.common.*
import org.ksharp.typesystem.*
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.CommonAttribute
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

@Suppress("DataClassPrivateConstructor")
data class TraitType private constructor(
    override val attributes: Set<Attribute>,
    val name: String,
    val param: String,
    val methods: Map<String, MethodType>,
) : Type, IsTrait {

    override lateinit var typeSystem: HandlePromise<TypeSystem>
        private set

    internal constructor(
        typeSystem: HandlePromise<TypeSystem>,
        attributes: Set<Attribute>,
        name: String,
        param: String,
        methods: Map<String, MethodType>,
    ) : this(
        attributes,
        name,
        param,
        methods
    ) {
        this.typeSystem = typeSystem
    }

    override val solver: Solver
        get() = Solvers.PassThrough
    override val serializer: TypeSerializer
        get() = TypeSerializers.TraitType

    override val unification: TypeUnification
        get() = TypeUnifications.Trait

    override val substitution: Substitution
        get() = Substitutions.NoDefined

    @Suppress("DataClassPrivateConstructor")
    data class MethodType private constructor(
        override val attributes: Set<Attribute>,
        val traitName: String,
        val name: String,
        override val arguments: List<Type>,
        val withDefaultImpl: Boolean,
    ) : FunctionType {
        override val scope: FunctionScope get() = FunctionScope(FunctionScopeType.Trait, traitName, null)
        
        override lateinit var typeSystem: HandlePromise<TypeSystem>
            private set

        internal constructor(
            typeSystem: HandlePromise<TypeSystem>,
            attributes: Set<Attribute>,
            traitName: String,
            name: String,
            arguments: List<Type>,
            withDefaultImpl: Boolean,
        ) : this(
            attributes,
            traitName,
            name,
            arguments,
            withDefaultImpl
        ) {
            this.typeSystem = typeSystem
        }

        override val solver: Solver
            get() = Solvers.NoDefined
        override val serializer: TypeSerializer
            get() = TypeSerializers.MethodType

        override val unification: TypeUnification
            get() = TypeUnifications.Method

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

        override fun new(attributes: Set<Attribute>): Type =
            MethodType(typeSystem, attributes, traitName, name, arguments, withDefaultImpl)
    }

    override val compound: Boolean
        get() = false

    override val terms: Sequence<Type>
        get() = methods.values.asSequence()

    override fun toString(): String = """
        |trait $name $param =
        |    ${methods.values.joinToString("\n    ") { it.representation }}
    """.trimMargin("|")

    override fun new(attributes: Set<Attribute>): Type = TraitType(typeSystem, attributes, name, param, methods)

}

class TraitTypeFactory(
    private val traitName: String,
    private val factory: TypeItemBuilder
) {
    private var result: ErrorOrValue<MapBuilder<String, TraitType.MethodType>> = Either.Right(mapBuilder())

    fun method(
        name: String,
        withDefaultImpl: Boolean = false,
        arguments: ParametricTypeFactoryBuilder = {}
    ) {
        result = result.flatMap { params ->
            validateFunctionName(name).flatMap {
                ParametricTypeFactory(factory).apply(arguments).build().flatMap traitMethod@{ args ->
                    val traitMethodName = "${name}/${args.arity}"
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
                            factory.handle,
                            setOf(CommonAttribute.TraitMethod),
                            traitName,
                            name,
                            args,
                            withDefaultImpl
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
                TraitType(handle, attributes, name, paramName, it)
            }
        }
    }

fun TraitType.toParametricType() =
    ParametricType(typeSystem, attributes, this, listOf(Parameter(typeSystem, param)))
