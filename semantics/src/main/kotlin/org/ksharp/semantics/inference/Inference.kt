package org.ksharp.semantics.inference

import org.ksharp.common.*
import org.ksharp.nodes.semantic.*
import org.ksharp.semantics.expressions.CollectionFunctionName
import org.ksharp.semantics.expressions.PRELUDE_COLLECTION_FLAG
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.solver.solve
import org.ksharp.typesystem.types.*
import org.ksharp.typesystem.unification.unify

enum class InferenceErrorCode(override val description: String) : ErrorCode {
    FunctionNotFound("Function '{function}' not found"),
    NoATuple("Type '{type}' is not a tuple"),
    NoAList("Type '{type}' is not a list"),
    IncompatibleType("Type '{type}' is not compatible in binding"),
    BindingUsedAsGuard("Binding used as guard")
}

private fun ApplicationName.calculateType(info: InferenceInfo): ErrorOrType =
    info.inferenceContext.typeSystem[name]

private fun SemanticNode<SemanticInfo>.isCollectionApplication(fnName: String): Boolean =
    this is ApplicationNode
            && this.cast<ApplicationNode<SemanticInfo>>().functionName == ApplicationName(
        PRELUDE_COLLECTION_FLAG,
        fnName
    )

internal val <T : SemanticInfo> AbstractionNode<T>.nameWithArity: String
    get() = info.cast<AbstractionSemanticInfo>().parameters.size.let { "$name/$it" }

private val SemanticNode<SemanticInfo>.isTuple: Boolean
    get() = isCollectionApplication("tupleOf")

private val SemanticNode<SemanticInfo>.isList: Boolean
    get() = isCollectionApplication("listOf")

private val SemanticNode<SemanticInfo>.isType: Boolean
    get() = this is ApplicationNode
            && this.cast<ApplicationNode<SemanticInfo>>().functionName.name.first().isUpperCase()

fun SemanticNode<SemanticInfo>.inferType(caller: String, info: InferenceInfo): ErrorOrType =
    if (this.info.hasInferredType()) {
        this.info.getInferredType(location)
    } else {
        when (this) {
            is AbstractionNode -> infer(this.nameWithArity, info).let {
                info.inferenceContext.unify(nameWithArity, location, it)
            }

            is ApplicationNode -> infer(caller, info)
            is ConstantNode -> infer()
            is VarNode -> infer()

            is LetNode -> infer(caller, info)
            is LetBindingNode -> infer(caller, info)

            is MatchNode -> infer(caller, info)
            is MatchBranchNode -> TODO()

            is ConditionalMatchValueNode -> {
                val boolType = info.prelude.typeSystem["Bool"].valueOrNull!!
                left.inferType(caller, info).flatMap { lType ->
                    lType.unify(location, boolType, info.checker).flatMap {
                        right.inferType(caller, info).flatMap { rType ->
                            rType.unify(location, boolType, info.checker)
                        }
                    }
                }
            }

            is ListMatchValueNode -> Either.Left(InferenceErrorCode.BindingUsedAsGuard.new(location))
        }.also {
            this.info.setInferredType(it.flatMap { t ->
                t.solve()
            })
        }
    }

private fun SemanticNode<SemanticInfo>.bindTuple(caller: String, type: Type, info: InferenceInfo): ErrorOrType =
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
                arg.bindType(caller, elementType, info)
            }.unwrap()
            .map { it.toTupleType(info.inferenceContext.typeSystem, type.attributes) }
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
        else info.getType(rootType)
            .flatMap {
                it.unify(location, type, info.checker).map { uType ->
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

private fun SemanticNode<SemanticInfo>.bindType(caller: String, type: Type, info: InferenceInfo): ErrorOrType =
    when {
        this is VarNode -> Either.Right(type)
        isTuple -> bindTuple(caller, type, info)
        isList -> bindList(type, info)
        this is ListMatchValueNode -> bindListWithTail(type, info)
        this is ConditionalMatchValueNode -> {
            left.bindType(caller, type, info).flatMap { bType ->
                left.info.setInferredType(Either.Right(bType))
                right.inferType(caller, info).flatMap {
                    it.unify(location, info.prelude.typeSystem["Bool"].valueOrNull!!, info.checker).map {
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
                    bindType.unify(location, type, info.checker)
                        .map { uType ->
                            val argType = Either.Right(uType)
                            appNode.arguments.forEach { arg -> arg.info.setInferredType(argType) }
                            uType
                        }
                }
        }

        else -> inferType(caller, info).flatMap {
            it.unify(location, type, info.checker)
        }
    }.also {
        this.info.setInferredType(it)
    }

private fun LetBindingNode<SemanticInfo>.infer(caller: String, info: InferenceInfo): ErrorOrType =
    expression.inferType(caller, info).flatMap {
        match.bindType(caller, it, info)
    }

private fun LetNode<SemanticInfo>.infer(caller: String, info: InferenceInfo): ErrorOrType =
    this.bindings.map {
        it.inferType(caller, info)
    }.firstOrNull { it.isLeft }
        ?: expression.inferType(caller, info)

private fun MatchNode<SemanticInfo>.infer(caller: String, info: InferenceInfo): ErrorOrType =
    this.expression.inferType(caller, info).flatMap { exprType ->
        this.branches.map { branch ->
            branch.infer(caller, info, exprType).also {
                branch.info.setInferredType(it)
            }
        }.unwrap()
            .flatMap {
                var result: ErrorOrType = Either.Right(it.first())
                for (right in it.drop(1)) {
                    result = result.flatMap { left ->
                        left.unify(location, right, info.checker)
                    }
                    if (result.isLeft) break
                }
                result
            }
    }

private fun MatchBranchNode<SemanticInfo>.infer(caller: String, info: InferenceInfo, exprType: Type): ErrorOrType =
    match.bindType(caller, exprType, info).flatMap {
        expression.inferType(caller, info)
    }


private fun AbstractionNode<SemanticInfo>.calculateFunctionType(
    isNative: Boolean,
    returnType: Type,
    info: InferenceInfo
) =
    this.info.cast<AbstractionSemanticInfo>().parameters.let { params ->
        if (params.isEmpty()) {
            info.prelude.typeSystem["Unit"].map { unitType ->
                listOf(unitType, returnType).toFunctionType(info.inferenceContext.typeSystem)
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
                    (it + returnType).toFunctionType(info.inferenceContext.typeSystem)
                }
        }
    }

private fun Type.toFixedTraitOrType(): Type =
    when (this) {
        is ImplType -> {
            this.solve().valueOrNull!!
        }

        is TraitType -> FixedTraitType(this)
        else -> this
    }

private fun AbstractionNode<SemanticInfo>.infer(caller: String, info: InferenceInfo): ErrorOrType =
    attributes.contains(CommonAttribute.Native).let { native ->
        run {
            if (native) {
                this.info.cast<AbstractionSemanticInfo>().returnType?.getType(location)
                    ?: Either.Left(SemanticInfoErrorCode.TypeNotInferred.new(location))
            } else expression.inferType(caller, info)
        }.flatMap { returnType ->
            if (expression is ApplicationNode) {
                val fn = expression.info.cast<ApplicationSemanticInfo>().function
                if (fn is PartialFunctionType) {
                    val abstractionInfo = this.info.cast<AbstractionSemanticInfo>()
                    val previousName = nameWithArity
                    if (abstractionInfo.parameters.isNotEmpty()) {
                        return@flatMap calculateFunctionType(native, fn, info)
                    }
                    abstractionInfo.updateParameters(fn)
                    info.inferenceContext.cast<CodeInferenceContext>()
                        .registerPartialFunctionAbstraction(previousName, this)
                }
            }
            calculateFunctionType(native, returnType.toFixedTraitOrType(), info)
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
    when (CollectionFunctionName.entries.first { it.applicationName == name }) {
        CollectionFunctionName.List, CollectionFunctionName.Set, CollectionFunctionName.Map -> {
            val unifiedType = reduceOrNull { acc, type ->
                acc.flatMap { left ->
                    type.flatMap { right ->
                        left.unify(location, right, info.checker)
                    }
                }
            }
            unifiedType?.let { sequenceOf(it) } ?: emptySequence()
        }

        CollectionFunctionName.Tuple -> sequenceOf(unwrap().map { it.toTupleType(info.inferenceContext.typeSystem) })
    }

private fun FunctionType.getOriginalFunction() =
    if (this is PartialFunctionType)
        this.function
    else this

private fun ApplicationNode<SemanticInfo>.infer(caller: String, info: InferenceInfo): ErrorOrType =
    functionName.pck.equals(PRELUDE_COLLECTION_FLAG).let { isPreludeCollectionFlag ->
        arguments.asSequence()
            .map {
                it.inferType(caller, info)
            }.let {
                if (isPreludeCollectionFlag) {
                    it.unifyArguments(functionName, location, info)
                } else it
            }.unwrap().flatMap {
                info.findAppType(caller, location, functionName, it, FindFunctionMode.Complete)
                    .flatMapLeft { e ->
                        info.findAppType(caller, location, functionName, it, FindFunctionMode.Partial)
                            .mapLeft { e }
                    }
                    .map { fn ->
                        if (fn is FunctionType) {
                            this.info.cast<ApplicationSemanticInfo>().function = fn
                            val inferredFn = fn.cast<FunctionType>()
                            if (!isPreludeCollectionFlag) {
                                fn.getOriginalFunction().arguments.asSequence()
                                    .zip(arguments.asSequence()) { fnArg, arg ->
                                        arg.info.setInferredType(fnArg.solve())
                                    }.last()
                            }
                            inferredFn.arguments.last()
                        } else fn
                    }
            }
    }
