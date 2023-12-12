package org.ksharp.typesystem.types

import org.ksharp.common.Either.Left
import org.ksharp.common.Either.Right
import org.ksharp.common.HandlePromise
import org.ksharp.common.new
import org.ksharp.typesystem.TypeItemBuilder
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.TypeSystemErrorCode.InvalidFunctionType
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

interface FunctionType : Type {
    val arguments: List<Type>
}

data class PartialFunctionType(
    override val arguments: List<Type>,
    val function: FunctionType
) : FunctionType {

    override val typeSystem: HandlePromise<TypeSystem> = function.typeSystem
    override val attributes: Set<Attribute> = function.attributes

    override val serializer: TypeSerializer = TypeSerializers.PartialFunctionType
    override val unification: TypeUnification = TypeUnifications.PartialFunction
    override val substitution: Substitution = Substitutions.PartialFunction
    override val solver: Solver = Solvers.PartialFunction

    override val terms: Sequence<Type> = arguments.asSequence()

    override fun new(attributes: Set<Attribute>): Type = this

    override fun toString(): String = arguments.asSequence().map { it.representation }.joinToString(" -> ")
}

@Suppress("DataClassPrivateConstructor")
data class FullFunctionType private constructor(
    override val attributes: Set<Attribute>,
    override val arguments: List<Type>,
) : FunctionType {
    override lateinit var typeSystem: HandlePromise<TypeSystem>
        private set

    internal constructor(
        typeSystem: HandlePromise<TypeSystem>,
        attributes: Set<Attribute>,
        arguments: List<Type>
    ) : this(
        attributes,
        arguments
    ) {
        this.typeSystem = typeSystem
    }

    override val solver: Solver
        get() = Solvers.Function
    override val serializer: TypeSerializer
        get() = TypeSerializers.FunctionType

    override val unification: TypeUnification
        get() = TypeUnifications.Function

    override val substitution: Substitution
        get() = Substitutions.Function

    override val terms: Sequence<Type>
        get() = arguments.asSequence()

    override fun toString(): String = arguments.asSequence().map { it.representation }.joinToString(" -> ")

    override fun new(attributes: Set<Attribute>): Type = FullFunctionType(typeSystem, attributes, arguments)
}

fun TypeItemBuilder.functionType(factory: ParametricTypeFactoryBuilder) =
    ParametricTypeFactory(this.createForSubtypes()).apply(factory).build().flatMap { args ->
        if (args.size < 2) {
            Left(InvalidFunctionType.new())
        } else Right(FullFunctionType(handle, attributes, args))
    }

fun List<Type>.toFunctionType(typeSystem: TypeSystem, attributes: Set<Attribute> = NoAttributes) =
    FullFunctionType(typeSystem.handle, attributes, this)

fun List<Type>.toFunctionType(handle: HandlePromise<TypeSystem>, attributes: Set<Attribute> = NoAttributes) =
    FullFunctionType(handle, attributes, this)
