package org.ksharp.semantics.inference

import org.ksharp.common.*
import org.ksharp.module.FunctionInfo
import org.ksharp.module.ModuleInfo
import org.ksharp.nodes.semantic.ApplicationName
import org.ksharp.nodes.semantic.SemanticNode
import org.ksharp.semantics.expressions.PRELUDE_COLLECTION_FLAG
import org.ksharp.semantics.nodes.SemanticInfo
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
): ErrorOrValue<FunctionType> {
    val context = SubstitutionContext(typeSystem)
    val result: ErrorOrValue<FunctionType>? = types.asSequence().zip(arguments.asSequence()) { item1, item2 ->
        val substitutionResult = context.extract(location, item1, item2)
        if (substitutionResult.isLeft) {
            incompatibleType<List<Type>>(location, item1, item2).cast<Either.Left<Error>>()
        } else substitutionResult
    }.firstNotNullOfOrNull { if (it.isLeft) it.cast<ErrorOrValue<FunctionType>>() else null }
    val fnType = types.toFunctionType(attributes)
    return result ?: context.substitute(location, fnType, fnType).cast()
}

internal fun FunctionInfo.unify(
    typeSystem: TypeSystem,
    location: Location,
    arguments: List<Type>
): ErrorOrValue<FunctionType> {
    return typeSystem(types.last()).flatMap { returnType ->
        types.asSequence().zip(arguments.asSequence()) { item1, item2 ->
            typeSystem.unify(location, item1, item2)
        }
            .unwrap()
            .flatMap { params ->
                if (returnType.parameters.firstOrNull() != null) {
                    substitute(typeSystem, location, params)
                } else {
                    Either.Right((params + returnType).toFunctionType(attributes))
                }
            }
    }
}

data class InferenceInfo(
    val prelude: InferenceModuleInfo,
    val module: InferenceModuleInfo,
    val dependencies: Map<String, ModuleInfo> = emptyMap()
) {
    private val cache = cacheOf<Pair<String, List<Type>>, Either<String, Type>>()

    private fun functionName(name: String, arguments: List<Type>) =
        "$name ${
            arguments.joinToString(" ") {
                it.representation
            }
        }"

    private fun FunctionInfo.infer() {
        if (this is AbstractionFunctionInfo) {
            this.abstraction
                .cast<SemanticNode<SemanticInfo>>()
                .inferType(this@InferenceInfo)
        }
    }

    private fun Sequence<FunctionInfo>?.unify(
        typeSystem: TypeSystem,
        location: Location,
        arguments: List<Type>
    ): Either.Right<FunctionType>? =
        if (this != null)
            this.map {
                it.infer()
                it.unify(typeSystem, location, arguments)
            }.firstOrNull { it.isRight }
                    as? Either.Right<FunctionType>
        else null

    fun findAppType(
        location: Location,
        appName: ApplicationName,
        arguments: List<Type>
    ): ErrorOrType =
        appName.name.let {
            if (it.first().isUpperCase()) {
                findConstructorType(location, appName, arguments)
            } else findFunctionType(location, appName, arguments)
        }

    private fun findFunctionType(
        location: Location,
        appName: ApplicationName,
        arguments: List<Type>
    ): ErrorOrType =
        arguments.size.let { numArguments ->
            val name = appName.name
            val funName = appName.pck?.let { if (it == PRELUDE_COLLECTION_FLAG) null else "$it.$name" } ?: name
            cache.get(funName to arguments) {
                val firstSearch = if (appName.pck == PRELUDE_COLLECTION_FLAG) prelude else module
                val secondSearch = if (appName.pck == null) prelude else null
                firstSearch.findFunction(name, numArguments + 1)
                    .unify(module.typeSystem, location, arguments)
                    ?: secondSearch?.findFunction(name, numArguments + 1)
                        .unify(prelude.typeSystem, location, arguments)
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
            cache.get(name to arguments) {
                val type = module.typeSystem[name]
                val result = if (type.isLeft) {
                    prelude.typeSystem[name]
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

}
