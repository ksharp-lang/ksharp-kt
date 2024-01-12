package org.ksharp.semantics.inference

import org.ksharp.common.*
import org.ksharp.module.FunctionInfo
import org.ksharp.nodes.semantic.ApplicationName
import org.ksharp.nodes.semantic.SemanticInfo
import org.ksharp.nodes.semantic.SemanticNode
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.TypeSystemErrorCode
import org.ksharp.typesystem.incompatibleType
import org.ksharp.typesystem.substitution.SubstitutionContext
import org.ksharp.typesystem.substitution.extract
import org.ksharp.typesystem.substitution.substitute
import org.ksharp.typesystem.types.*
import org.ksharp.typesystem.unification.UnificationChecker
import org.ksharp.typesystem.unification.unify

private data class FindFunctionKey(
    val name: String,
    val arguments: List<Type>,
    val mode: FindFunctionMode
)

private val noFoundError =
    Either.Left(TypeSystemErrorCode.TypeNotFound.new("type" to "<>"))
private val ApplicationName.functionName
    get() =
        if (pck == null) name else "$pck.$name"

internal fun FunctionInfo.substitute(
    checker: UnificationChecker,
    typeSystem: TypeSystem,
    location: Location,
    arguments: List<Type>
): ErrorOrValue<FunctionType> {
    val context = SubstitutionContext(checker)
    val result: ErrorOrValue<FunctionType>? = types.asSequence().zip(arguments.asSequence()) { item1, item2 ->
        val substitutionResult = context.extract(location, item1, item2)
        if (substitutionResult.isLeft) {
            incompatibleType<List<Type>>(location, item1, item2).cast<Either.Left<Error>>()
        } else substitutionResult
    }.firstNotNullOfOrNull { if (it.isLeft) it.cast<ErrorOrValue<FunctionType>>() else null }
    val fnType = types.toFunctionType(typeSystem, attributes)
    return result ?: context.substitute(location, fnType, fnType).cast()
}

internal fun FunctionInfo.unify(
    checker: UnificationChecker,
    typeSystem: TypeSystem,
    location: Location,
    arguments: List<Type>,
    mode: FindFunctionMode
): ErrorOrType {
    return types.last()().flatMap { returnType ->
        types.asSequence().zip(arguments.asSequence()) { item1, item2 ->
            item1.unify(location, item2, checker)
        }
            .unwrap()
            .flatMap { unifiedParams ->
                val params = if (mode == FindFunctionMode.Partial) {
                    unifiedParams + types.drop(unifiedParams.size).dropLast(1)
                } else unifiedParams
                val result = if (returnType.parameters.firstOrNull() != null) {
                    substitute(checker, typeSystem, location, params)
                } else {
                    Either.Right((params + returnType).toFunctionType(typeSystem, attributes))
                }
                result.map {
                    if (mode == FindFunctionMode.Partial) {
                        PartialFunctionType(it.arguments.drop(arguments.size), it)
                    } else it
                }
            }
    }
}

internal fun Sequence<FunctionInfo>.unify(
    checker: UnificationChecker,
    typeSystem: TypeSystem,
    location: Location,
    arguments: List<Type>,
    mode: FindFunctionMode
): ErrorOrType? {
    var firstResult: ErrorOrType? = null
    for (item in map { it.unify(checker, typeSystem, location, arguments, mode) }) {
        if (firstResult == null) {
            firstResult = item
        }
        if (item.isRight) return item
    }
    return firstResult
}

data class InferenceInfo(
    val prelude: InferenceContext,
    val inferenceContext: InferenceContext,
    val dependencies: Map<String, InferenceContext>
) {
    private val cache = cacheOf<FindFunctionKey, Either<String, Type>>()

    val checker: UnificationChecker get() = inferenceContext.checker

    private fun functionName(name: String, arguments: List<Type>) =
        "$name ${
            arguments.joinToString(" ") {
                it.representation
            }
        }"

    private val ApplicationName.firstInferenceContext
        get() =
            when (pck) {
                null -> inferenceContext
                else -> dependencies[pck]
            }

    private val ApplicationName.secondInferenceContext
        get() =
            if (pck == null) prelude else null

    private fun FunctionInfo.infer(caller: String): FunctionInfo {
        if (this is AbstractionFunctionInfo) {
            this.abstraction
                .cast<SemanticNode<SemanticInfo>>()
                .inferType(caller, this@InferenceInfo)
        }
        return this
    }

    private fun Sequence<FunctionInfo>.infer(caller: String): Sequence<FunctionInfo> =
        this.map { it.infer(caller) }

    fun findAppType(
        caller: String,
        location: Location,
        appName: ApplicationName,
        arguments: List<Type>,
        mode: FindFunctionMode
    ): ErrorOrType =
        appName.name.let {
            if (it.first().isUpperCase()) {
                findConstructorType(location, appName, arguments)
            } else findFunctionType(caller, location, appName, arguments, mode)
        }

    private fun findFunctionType(
        caller: String,
        location: Location,
        appName: ApplicationName,
        arguments: List<Type>,
        mode: FindFunctionMode
    ): ErrorOrType =
        when (val size = arguments.size) {
            1 -> if (arguments.first().isUnitType) 0 else 1
            else -> size
        }.let { numArguments ->
            val name = appName.name
            cache.get(FindFunctionKey(appName.functionName, arguments, mode)) {
                val firstArgument = arguments.first()
                val firstSearch = appName.firstInferenceContext
                val secondSearch = appName.secondInferenceContext
                firstSearch?.findFunction(caller, name, numArguments, firstArgument, mode)
                    ?.infer(caller)
                    ?.unify(checker, inferenceContext.typeSystem, location, arguments, mode)
                    ?.mapLeft { it.toString() }
                    ?: secondSearch?.findFunction(caller, name, numArguments, firstArgument, mode)
                        ?.infer(caller)
                        ?.unify(checker, prelude.typeSystem, location, arguments, mode)
                        ?.mapLeft { it.toString() }
                    ?: Either.Left(functionName(name, arguments))
            }.mapLeft {
                InferenceErrorCode.FunctionNotFound.new(
                    location,
                    "function" to it
                )
            }
        }

    private fun findConstructorType(
        location: Location,
        appName: ApplicationName,
        arguments: List<Type>
    ): ErrorOrType =
        arguments.size.let { _ ->
            val name = appName.name
            cache.get(FindFunctionKey(appName.functionName, arguments, FindFunctionMode.Complete)) {
                val type = appName.firstInferenceContext?.typeSystem?.get(name) ?: noFoundError
                val result = if (type.isLeft) {
                    appName.secondInferenceContext?.typeSystem?.get(name) ?: noFoundError
                } else type
                result.mapLeft {
                    functionName(name, arguments)
                }
            }.mapLeft {
                InferenceErrorCode.FunctionNotFound.new(
                    location,
                    "function" to it
                )
            }
        }

    fun getType(name: String): ErrorOrType =
        inferenceContext.typeSystem[name]
}
