package org.ksharp.semantics.inference

import InferenceErrorCode
import org.ksharp.common.*
import org.ksharp.module.FunctionInfo
import org.ksharp.module.ModuleInfo
import org.ksharp.nodes.semantic.ApplicationName
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.incompatibleType
import org.ksharp.typesystem.types.Parameter
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.unification.unify


internal fun Parameter.substitute(
    typeSystem: TypeSystem,
    location: Location,
    types: List<Type>,
    arguments: List<Type>
): ErrorOrValue<List<Type>> {
    val substitutions = mapBuilder<String, Type>()
    val typesIter = types.iterator()
    val argumentsIter = arguments.iterator()
    var result: ErrorOrValue<List<Type>>? = null
    while (argumentsIter.hasNext() && typesIter.hasNext()) {
        val item1 = typesIter.next()
        val item2 = argumentsIter.next()
        if (item1 is Parameter) {
            val substitutionType = substitutions.get(item1.name)?.let {
                val paramUnification = typeSystem.unify(location, it, item2)
                if (paramUnification.isLeft) typeSystem.unify(location, item2, it)
                else paramUnification
            } ?: Either.Right(item2)
            if (substitutionType.isLeft) {
                result = incompatibleType<List<Type>>(location, item1, item2)
                    .cast<Either.Left<org.ksharp.common.Error>>()
                break
            }
            substitutions.put(item1.name, substitutionType.cast<Either.Right<Type>>().value)
        }
    }
    @Suppress("NAME_SHADOWING")
    return result ?: run {
        val functionTypes = listBuilder<Type>()
        val typesIter = types.iterator()
        val argumentsIter = arguments.iterator()
        while (typesIter.hasNext() && argumentsIter.hasNext()) {
            val item1 = typesIter.next()
            val item2 = argumentsIter.next()
            functionTypes.add(
                if (item1 is Parameter) {
                    substitutions.get(item1.name)!!
                } else item2
            )
        }
        functionTypes.add(substitutions.get(name)!!)
        Either.Right(functionTypes.build())
    }
}

internal fun FunctionInfo.unify(
    typeSystem: TypeSystem,
    location: Location,
    arguments: List<Type>
): ErrorOrValue<List<Type>> {
    return typeSystem(types.last()).flatMap { returnType ->
        val typesIter = types.iterator()
        val argumentsIter = arguments.iterator()
        var result: ErrorOrValue<List<Type>>? = null
        val params = listBuilder<Type>()
        while (argumentsIter.hasNext() && typesIter.hasNext()) {
            val item1 = typesIter.next()
            val item2 = argumentsIter.next()
            val unifyItem = typeSystem.unify(location, item1, item2)
            if (unifyItem.isLeft) {
                result = incompatibleType<List<Type>>(location, item1, item2)
                    .cast<Either.Left<org.ksharp.common.Error>>()
                break
            }
            params.add(unifyItem.cast<Either.Right<Type>>().value)
        }
        result ?: run {
            if (returnType is Parameter) {
                val argumentsUnified = params.build()
                returnType.substitute(typeSystem, location, types, argumentsUnified)
            } else {
                params.add(returnType)
                Either.Right(params.build())
            }
        }
    }
}

data class InferenceInfo(
    val prelude: ModuleInfo,
    val module: ModuleInfo,
    val dependencies: Map<String, ModuleInfo> = emptyMap()
) {
    private val cache = cacheOf<Pair<String, List<Type>>, Either<String, List<Type>>>()

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
    ): Either.Right<List<Type>>? =
        this?.map { it.unify(typeSystem, location, arguments) }
            ?.firstOrNull { it.isRight }
            ?.cast<Either.Right<List<Type>>>()

    fun findFunction(
        location: Location,
        appName: ApplicationName,
        arguments: List<Type>
    ): ErrorOrValue<List<Type>> =
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