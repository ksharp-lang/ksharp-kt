package org.ksharp.semantics.inference

import org.ksharp.common.*
import org.ksharp.nodes.semantic.*
import org.ksharp.semantics.expressions.CollectionFunctionName
import org.ksharp.semantics.expressions.PRELUDE_COLLECTION_FLAG
import org.ksharp.semantics.nodes.AbstractionSemanticInfo
import org.ksharp.semantics.nodes.ApplicationSemanticInfo
import org.ksharp.semantics.nodes.SemanticInfo
import org.ksharp.semantics.nodes.getType
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.types.*
import org.ksharp.typesystem.unification.unify

enum class InferenceErrorCode(override val description: String) : ErrorCode {
    TypeNotInferred("Type not inferred"),
    FunctionNotFound("Function '{function}' not found"),
    NoATuple("Type '{type}' is not a tuple"),
    NoAList("Type '{type}' is not a list"),
    IncompatibleType("Type '{type}' is not compatible in binding"),
    BindingUsedAsGuard("Binding used as guard")
}

private fun ApplicationName.calculateType(info: InferenceInfo): ErrorOrType =
    info.module.typeSystem[name].flatMapLeft {
        info.prelude.typeSystem[name]
    }

private fun ApplicationName.getTypeSystem(info: InferenceInfo): TypeSystem =
    info.module.typeSystem

private fun SemanticNode<SemanticInfo>.isCollectionApplication(fnName: String): Boolean =
    this is ApplicationNode
            && this.cast<ApplicationNode<SemanticInfo>>().functionName == ApplicationName(
        PRELUDE_COLLECTION_FLAG,
        fnName
    )

private val SemanticNode<SemanticInfo>.isTuple: Boolean
    get() = isCollectionApplication("tupleOf")

private val SemanticNode<SemanticInfo>.isList: Boolean
    get() = isCollectionApplication("listOf")

private val SemanticNode<SemanticInfo>.isType: Boolean
    get() = this is ApplicationNode
            && this.cast<ApplicationNode<SemanticInfo>>().functionName.name.first().isUpperCase()

fun SemanticNode<SemanticInfo>.inferType(info: InferenceInfo): ErrorOrType =
    if (this.info.hasInferredType()) {
        this.info.getInferredType(location)
    } else {
        when (this) {
            is AbstractionNode -> infer(info)
            is ApplicationNode -> infer(info)
            is ConstantNode -> infer()
            is VarNode -> infer()

            is LetNode -> infer(info)
            is LetBindingNode -> infer(info)

            is MatchNode -> TODO()
            is MatchBranchNode -> TODO()

            is ConditionalMatchValueNode -> {
                val ts = info.module.typeSystem
                val boolType = info.prelude.typeSystem["Bool"].valueOrNull!!
                left.inferType(info).flatMap { lType ->
                    ts.unify(location, lType, boolType).flatMap {
                        right.inferType(info).flatMap { rType ->
                            ts.unify(location, rType, boolType)
                        }
                    }
                }
            }

            is ListMatchValueNode -> Either.Left(InferenceErrorCode.BindingUsedAsGuard.new(location))
        }.also { this.info.setInferredType(it) }
    }

private fun SemanticNode<SemanticInfo>.bindTuple(type: Type, info: InferenceInfo): ErrorOrType =
    if (type !is TupleType)
        InferenceErrorCode.NoATuple.new(location, "type" to type.representation)
            .let { Either.Left(it) }
            .cast()
    else {
        val tupleElements = type.cast<TupleType>().elements
        val bindArguments = this.cast<ApplicationNode<SemanticInfo>>().arguments

        if (tupleElements.size != bindArguments.size)
            InferenceErrorCode.IncompatibleType
                .new(location, "type" to type.representation)
                .let { Either.Left(it) }
                .cast()
        else bindArguments.zip(tupleElements)
            .map { (arg, elementType) ->
                arg.bindType(elementType, info)
            }.unwrap()
            .map { it.toTupleType(type.attributes) }
    }

private fun SemanticNode<SemanticInfo>.bindParametricType(
    type: Type,
    info: InferenceInfo,
    rootType: String,
    expectedParamsSize: Int,
    errorCode: InferenceErrorCode,
    bind: (type: ParametricType) -> Unit
): ErrorOrType =
    if (type !is ParametricType)
        errorCode.new(location, "type" to type.representation)
            .let { Either.Left(it) }
            .cast()
    else {
        val paramTypes = type.cast<ParametricType>().params

        if (paramTypes.size != expectedParamsSize)
            errorCode
                .new(location, "type" to type.representation)
                .let { Either.Left(it) }
                .cast()
        else info.prelude.typeSystem[rootType]
            .flatMap {
                info.module.typeSystem.unify(location, it, type).map { uType ->
                    bind(uType.cast())
                    uType
                }
            }
    }

private fun SemanticNode<SemanticInfo>.bindList(type: Type, info: InferenceInfo): ErrorOrType =
    bindParametricType(type, info, "List", 1, InferenceErrorCode.NoAList) {
        val argType = Either.Right(it.params[0])
        this.cast<ApplicationNode<SemanticInfo>>()
            .arguments
            .forEach { arg -> arg.info.setInferredType(argType) }
    }

private fun SemanticNode<SemanticInfo>.bindListWithTail(type: Type, info: InferenceInfo): ErrorOrType =
    bindParametricType(type, info, "List", 1, InferenceErrorCode.NoAList) {
        val argType = Either.Right(it.params[0])
        this.cast<ListMatchValueNode<SemanticInfo>>().let { match ->
            match.head.forEach { arg -> arg.info.setInferredType(argType) }
            match.tail.info.setInferredType(Either.Right(it))
        }
    }

private fun SemanticNode<SemanticInfo>.bindType(type: Type, info: InferenceInfo): ErrorOrType =
    when {
        this is VarNode -> Either.Right(type)
        isTuple -> bindTuple(type, info)
        isList -> bindList(type, info)
        this is ListMatchValueNode -> bindListWithTail(type, info)
        this is ConditionalMatchValueNode -> {
            left.bindType(type, info).flatMap { bType ->
                left.info.setInferredType(Either.Right(bType))
                right.inferType(info).flatMap {
                    info.module.typeSystem.unify(location, it, info.prelude.typeSystem["Bool"].valueOrNull!!).map {
                        bType
                    }
                }
            }
        }

        isType -> {
            val appNode = this.cast<ApplicationNode<SemanticInfo>>()
            appNode.functionName
                .calculateType(info)
                .flatMap { bindType ->
                    appNode.functionName
                        .getTypeSystem(info)
                        .unify(location, bindType, type)
                        .map { uType ->
                            val argType = Either.Right(uType)
                            appNode.arguments.forEach { arg -> arg.info.setInferredType(argType) }
                            uType
                        }
                }
        }

        else -> inferType(info).flatMap {
            info.module.typeSystem.unify(location, it, type)
        }
    }.also {
        this.info.setInferredType(it)
    }

private fun LetBindingNode<SemanticInfo>.infer(info: InferenceInfo): ErrorOrType =
    expression.inferType(info).flatMap {
        match.bindType(it, info)
    }

private fun LetNode<SemanticInfo>.infer(info: InferenceInfo): ErrorOrType =
    this.bindings.map {
        it.inferType(info)
    }.firstOrNull { it.isLeft }
        ?: expression.inferType(info)

private fun AbstractionNode<SemanticInfo>.calculateFunctionType(
    isNative: Boolean,
    returnType: Type,
    info: InferenceInfo
) =
    this.info.cast<AbstractionSemanticInfo>().parameters.let { params ->
        if (params.isEmpty()) {
            info.prelude.typeSystem["Unit"].map { unitType ->
                listOf(unitType, returnType).toFunctionType()
            }
        } else {
            params.asSequence().run {
                if (isNative) {
                    map {
                        it.getType(location)
                            .also { t -> it.setInferredType(t) }
                    }
                } else map {
                    if (it.hasInferredType()) {
                        it.getInferredType(location)
                    } else {
                        it.getType(location).let { cType ->
                            it.setInferredType(cType)
                            cType
                        }
                    }
                }
            }.unwrap()
                .map {
                    (it + returnType).toFunctionType()
                }
        }
    }


private fun AbstractionNode<SemanticInfo>.infer(info: InferenceInfo): ErrorOrType =
    attributes.contains(CommonAttribute.Native).let { native ->
        run {
            if (native) {
                this.info.cast<AbstractionSemanticInfo>().returnType?.getType(location)
                    ?: Either.Left(InferenceErrorCode.TypeNotInferred.new(location))
            } else expression.inferType(info)
        }.flatMap { returnType ->
            calculateFunctionType(native, returnType, info)
        }
    }


private fun ConstantNode<SemanticInfo>.infer(): ErrorOrType =
    info.getType(location)

private fun VarNode<SemanticInfo>.infer(): ErrorOrType =
    info.getType(location)

private fun Sequence<ErrorOrType>.unifyArguments(
    name: ApplicationName,
    location: Location,
    info: InferenceInfo
): Sequence<ErrorOrType> =
    when (CollectionFunctionName.values().first { it.applicationName == name }) {
        CollectionFunctionName.List, CollectionFunctionName.Set, CollectionFunctionName.Map -> {
            val typeSystem = info.module.typeSystem
            val unifiedType = reduceOrNull { acc, type ->
                acc.flatMap { left ->
                    type.flatMap { right ->
                        typeSystem.unify(location, left, right)
                    }
                }
            }
            unifiedType?.let { sequenceOf(it) } ?: emptySequence()
        }

        CollectionFunctionName.Tuple -> sequenceOf(unwrap().map { it.toTupleType() })
    }

private fun ApplicationNode<SemanticInfo>.infer(info: InferenceInfo): ErrorOrType =
    functionName.pck.equals(PRELUDE_COLLECTION_FLAG).let { isPreludeCollectionFlag ->
        arguments.asSequence()
            .map {
                it.inferType(info)
            }.let {
                if (isPreludeCollectionFlag) {
                    it.unifyArguments(functionName, location, info)
                } else it
            }.unwrap().flatMap {
                info.findAppType(location, functionName, it).map { fn ->
                    if (fn is FunctionType) {
                        this.info.cast<ApplicationSemanticInfo>().function = fn
                        val inferredFn = fn.cast<FunctionType>()
                        if (!isPreludeCollectionFlag) {
                            inferredFn.arguments.asSequence()
                                .zip(arguments.asSequence()) { fnArg, arg ->
                                    arg.info.setInferredType(Either.Right(fnArg))
                                }.last()
                        }
                        inferredFn.arguments.last()
                    } else fn
                }
            }
    }
