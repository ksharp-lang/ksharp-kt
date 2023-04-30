package org.ksharp.semantics.inference

import InferenceErrorCode
import org.ksharp.common.*
import org.ksharp.module.FunctionInfo
import org.ksharp.module.ModuleInfo
import org.ksharp.nodes.semantic.ApplicationName
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.incompatibleType
import org.ksharp.typesystem.substitution.SubstitutionContext
import org.ksharp.typesystem.substitution.extract
import org.ksharp.typesystem.substitution.substitute
import org.ksharp.typesystem.types.FunctionType
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.types.parameters
import org.ksharp.typesystem.types.toFunctionType
import org.ksharp.typesystem.unification.unify

internal fun FunctionInfo.substitute(
    typeSystem: TypeSystem,
    location: Location,
    arguments: List<Type>
): ErrorOrType {
    val context = SubstitutionContext(typeSystem)
    val typesIter = types.iterator()
    val argumentsIter = arguments.iterator()
    var result: ErrorOrType? = null
    while (argumentsIter.hasNext() && typesIter.hasNext()) {
        val item1 = typesIter.next()
        val item2 = argumentsIter.next()
        val substitutionResult = context.extract(location, item1, item2)
        if (substitutionResult.isLeft) {
            result = incompatibleType<List<Type>>(location, item1, item2)
                .cast<Either.Left<Error>>()
            break
        }
    }
    val fnType = types.toFunctionType()
    return result ?: context.substitute(location, fnType, fnType)
}

internal fun FunctionInfo.unify(
    typeSystem: TypeSystem,
    location: Location,
    arguments: List<Type>
): ErrorOrType {
    return typeSystem(types.last()).flatMap { returnType ->
        val typesIter = types.iterator()
        val argumentsIter = arguments.iterator()
        var result: ErrorOrValue<FunctionType>? = null
        val params = listBuilder<Type>()
        while (argumentsIter.hasNext() && typesIter.hasNext()) {
            val item1 = typesIter.next()
            val item2 = argumentsIter.next()
            val unifyItem = typeSystem.unify(location, item1, item2)
            if (unifyItem.isLeft) {
                result = incompatibleType<FunctionType>(location, item1, item2)
                    .cast<Either.Left<Error>>()
                break
            }
            params.add(unifyItem.cast<Either.Right<Type>>().value)
        }
        result ?: run {
            if (returnType.parameters.firstOrNull() != null) {
                val argumentsUnified = params.build()
                substitute(typeSystem, location, argumentsUnified)
            } else {
                params.add(returnType)
                Either.Right(params.build().toFunctionType())
            }
        }
    }
}

data class InferenceInfo(
    val prelude: ModuleInfo,
    val module: ModuleInfo,
    val dependencies: Map<String, ModuleInfo> = emptyMap()
) {
    private val cache = cacheOf<Pair<String, List<Type>>, Either<String, Type>>()

    private fun ModuleInfo.findFunction(name: String, numParams: Int): Sequence<FunctionInfo>? =
        functions.asSequence().find {
            it.key == name
        }?.value?.asSequence()
            ?.filter { it.types.size == numParams }

    private fun functionName(name: String, arguments: List<Type>) =
        "$name ${
            arguments.joinToString(" ") {
                it.representation
            }
        }"

    private fun Sequence<FunctionInfo>?.unify(
        typeSystem: TypeSystem,
        location: Location,
        arguments: List<Type>
    ): Either.Right<FunctionType>? =
        this?.map { it.unify(typeSystem, location, arguments) }
            ?.firstOrNull { it.isRight }
            ?.cast<Either.Right<FunctionType>>()

    fun findFunction(
        location: Location,
        appName: ApplicationName,
        arguments: List<Type>
    ): ErrorOrType =
        arguments.size.let { numArguments ->
            val name = appName.name
            cache.get(name to arguments) {
                module.findFunction(name, numArguments + 1)
                    ?.unify(module.typeSystem, location, arguments)
                    ?: prelude.findFunction(name, numArguments + 1)
                        ?.unify(prelude.typeSystem, location, arguments)
                    ?: Either.Left(functionName(name, arguments))
            }.mapLeft {
                InferenceErrorCode.FunctionNotFound.new(
                    location,
                    "function" to it
                )
            }
        }
}