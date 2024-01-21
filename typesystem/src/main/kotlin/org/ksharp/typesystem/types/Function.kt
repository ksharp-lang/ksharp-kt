package org.ksharp.typesystem.types

import org.ksharp.common.Either.Left
import org.ksharp.common.Either.Right
import org.ksharp.common.HandlePromise
import org.ksharp.common.new
import org.ksharp.typesystem.TypeItemBuilder
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.TypeSystemErrorCode.InvalidFunctionType
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.serializer.TypeSerializer
import org.ksharp.typesystem.serializer.TypeSerializers
import org.ksharp.typesystem.solver.Solver
import org.ksharp.typesystem.solver.Solvers
import org.ksharp.typesystem.substitution.Substitution
import org.ksharp.typesystem.substitution.Substitutions
import org.ksharp.typesystem.unification.TypeUnification
import org.ksharp.typesystem.unification.TypeUnifications

enum class FunctionScopeType {
    Module,
    Trait,
    Impl
}

data class FunctionScope(
    val type: FunctionScopeType,
    val trait: String?,
    val impl: String?
)

val ModuleFunctionScope = FunctionScope(FunctionScopeType.Module, null, null)

interface FunctionType : Type {
    val scope: FunctionScope
    val arguments: List<Type>
}

data class PartialFunctionType(
    override val arguments: List<Type>,
    val function: FunctionType
) : FunctionType {

    override val scope: FunctionScope = function.scope
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
    override val scope: FunctionScope
) : FunctionType {

    override lateinit var typeSystem: HandlePromise<TypeSystem>
        private set

    internal constructor(
        typeSystem: HandlePromise<TypeSystem>,
        attributes: Set<Attribute>,
        arguments: List<Type>,
        scope: FunctionScope
    ) : this(
        attributes,
        arguments,
        scope
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

    override fun new(attributes: Set<Attribute>): Type = FullFunctionType(typeSystem, attributes, arguments, scope)
}

fun TypeItemBuilder.functionType(factory: ParametricTypeFactoryBuilder) =
    ParametricTypeFactory(this.createForSubtypes()).apply(factory).build().flatMap { args ->
        if (args.size < 2) {
            Left(InvalidFunctionType.new())
        } else Right(FullFunctionType(handle, attributes, args, FunctionScope(FunctionScopeType.Module, null, null)))
    }

fun List<Type>.toFunctionType(
    typeSystem: TypeSystem,
    attributes: Set<Attribute>,
    scope: FunctionScope
) =
    FullFunctionType(typeSystem.handle, attributes, this, scope)

fun List<Type>.toFunctionType(
    handle: HandlePromise<TypeSystem>,
    attributes: Set<Attribute>,
    scope: FunctionScope
) =
    FullFunctionType(handle, attributes, this, scope)
