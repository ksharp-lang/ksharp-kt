package org.ksharp.typesystem.types

import org.ksharp.common.Either.Left
import org.ksharp.common.Either.Right
import org.ksharp.common.new
import org.ksharp.typesystem.TypeItemBuilder
import org.ksharp.typesystem.TypeSystemErrorCode.InvalidFunctionType
import org.ksharp.typesystem.serializer.TypeSerializer
import org.ksharp.typesystem.serializer.TypeSerializers

data class FunctionType internal constructor(
    val arguments: List<Type>,
) : Type {
    override val serializer: TypeSerializer
        get() = TypeSerializers.FunctionType
    override val terms: Sequence<Type>
        get() = arguments.asSequence()

    override fun toString(): String = arguments.asSequence().map { it.representation }.joinToString(" -> ")
}

fun TypeItemBuilder.functionType(factory: ParametricTypeFactoryBuilder) =
    ParametricTypeFactory(this).apply(factory).build().flatMap { args ->
        if (args.size < 2) {
            Left(InvalidFunctionType.new())
        } else Right(FunctionType(args))
    }