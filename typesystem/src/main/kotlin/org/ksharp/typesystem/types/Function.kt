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

data class FunctionType internal constructor(
    override val typeSystem: HandlePromise<TypeSystem>,
    override val attributes: Set<Attribute>,
    val arguments: List<Type>,
) : Type {
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

    override fun new(attributes: Set<Attribute>): Type = FunctionType(typeSystem, attributes, arguments)
}

fun TypeItemBuilder.functionType(factory: ParametricTypeFactoryBuilder) =
    ParametricTypeFactory(this.createForSubtypes()).apply(factory).build().flatMap { args ->
        if (args.size < 2) {
            Left(InvalidFunctionType.new())
        } else Right(FunctionType(handle, attributes, args))
    }

fun List<Type>.toFunctionType(typeSystem: TypeSystem, attributes: Set<Attribute> = NoAttributes) =
    FunctionType(typeSystem.handle, attributes, this)

fun List<Type>.toFunctionType(handle: HandlePromise<TypeSystem>, attributes: Set<Attribute> = NoAttributes) =
    FunctionType(handle, attributes, this)
