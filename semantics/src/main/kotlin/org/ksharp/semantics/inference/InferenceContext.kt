package org.ksharp.semantics.inference

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.module.FunctionInfo
import org.ksharp.module.Impl
import org.ksharp.module.ModuleInfo
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.nodes.semantic.AbstractionSemanticInfo
import org.ksharp.nodes.semantic.SemanticInfo
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.types.FunctionType
import org.ksharp.typesystem.types.TraitType
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.types.arity
import org.ksharp.typesystem.unification.UnificationChecker
import org.ksharp.typesystem.unification.unify

typealias AbstractionNodeMap = MutableMap<String, AbstractionNode<AbstractionSemanticInfo>>

enum class FindFunctionMode {
    Partial,
    Complete
}

private inline fun run(sameToCaller: Boolean, action: () -> FunctionInfo?): FunctionInfo? =
    if (sameToCaller) null else action()

private fun AbstractionNodeMap.findPartialFunction(
    name: String,
    numParams: Int
) =
    "$name/".let { prefixName ->
        asSequence()
            .filter { (key, value) ->
                key.startsWith(prefixName) && value.info.parameters.size > numParams
            }.map {
                AbstractionFunctionInfo(it.value)
            }
    }

sealed class InferenceContext {
    abstract val traitFinderContext: TraitFinderContext

    val checker: UnificationChecker by lazy { unificationChecker(traitFinderContext) }

    val typeSystem by lazy { traitFinderContext.typeSystem }

    abstract fun findPartialFunction(
        caller: String,
        name: String,
        numParams: Int,
        firstArgument: Type
    ): Sequence<FunctionInfo>

    abstract fun findFullFunction(
        caller: String,
        name: String,
        numParams: Int,
        firstArgument: Type
    ): Sequence<FunctionInfo>?

    fun findFunction(caller: String, name: String, numParams: Int, firstArgument: Type, mode: FindFunctionMode) =
        when (mode) {
            FindFunctionMode.Partial -> findPartialFunction(caller, name, numParams, firstArgument)
            FindFunctionMode.Complete -> findFullFunction(caller, name, numParams, firstArgument)
        }

    abstract fun unify(name: String, location: Location, type: ErrorOrType): ErrorOrType
    fun methodName(name: String, numParams: Int) = "$name/$numParams"
}

class ModuleInfoInferenceContext(
    private val moduleInfo: ModuleInfo,
    override val traitFinderContext: TraitFinderContext = moduleInfo.traitFinderContext,
) :
    InferenceContext() {

    override fun findFullFunction(
        caller: String,
        name: String,
        numParams: Int,
        firstArgument: Type
    ): Sequence<FunctionInfo>? =
        methodName(name, numParams).let { methodName ->
            moduleInfo.functions[methodName] ?: traitFinderContext.findTraitFunction(methodName, firstArgument)
        }?.let { sequenceOf(it) }

    override fun findPartialFunction(
        caller: String,
        name: String,
        numParams: Int,
        firstArgument: Type
    ): Sequence<FunctionInfo> {
        return "$name/".let { prefixName ->
            sequenceOf(
                moduleInfo.functions
                    .asSequence()
                    .filter { (key, value) ->
                        key.startsWith(prefixName) && value.arity > numParams
                    }.map { it.value },
                traitFinderContext.findPartialTraitFunction(name, numParams, firstArgument)
            ).flatten()
        }
    }

    override fun unify(name: String, location: Location, type: ErrorOrType): ErrorOrType = type
}

class SemanticModuleInfoInferenceContext(
    override val traitFinderContext: TraitFinderContext,
    private val abstractions: AbstractionNodeMap
) : InferenceContext(), CodeInferenceContext by AbstractionsCodeInferenceContext(abstractions) {
    override fun findFullFunction(
        caller: String,
        name: String,
        numParams: Int,
        firstArgument: Type
    ): Sequence<FunctionInfo>? =
        (methodName(name, numParams) to "$name/0").let { (methodName, methodNameArityZero) ->
            sequenceOf(
                abstractions[methodName]?.let {
                    AbstractionFunctionInfo(it)
                },
                abstractions[methodNameArityZero]?.let {
                    AbstractionFunctionInfo(it)
                },
                traitFinderContext.findTraitFunction(methodName, firstArgument)
            ).filterNotNull()
        }.takeIf { it.firstOrNull() != null }

    override fun unify(name: String, location: Location, type: ErrorOrType): ErrorOrType = type

    override fun findPartialFunction(
        caller: String,
        name: String,
        numParams: Int,
        firstArgument: Type
    ): Sequence<FunctionInfo> =
        sequenceOf(
            abstractions.findPartialFunction(name, numParams),
            traitFinderContext.findPartialTraitFunction(name, numParams, firstArgument)
        ).flatten()

}

sealed class ObjectInferenceContext(
    private val traitType: TraitType,
) : InferenceContext() {
    override fun unify(name: String, location: Location, type: ErrorOrType): ErrorOrType =
        type.flatMap { t ->
            traitType.methods[name]?.unify(location, t, checker) ?: type
        }
}

class TraitInferenceContext(
    private val parent: InferenceContext,
    traitType: TraitType,
    private val abstractions: AbstractionNodeMap
) : ObjectInferenceContext(traitType), CodeInferenceContext by AbstractionsCodeInferenceContext(abstractions) {

    override val traitFinderContext: TraitFinderContext
            by lazy { parent.traitFinderContext }

    override fun findFullFunction(
        caller: String,
        name: String,
        numParams: Int,
        firstArgument: Type
    ): Sequence<FunctionInfo>? =
        methodName(name, numParams).let { methodName ->
            abstractions[methodName]?.let {
                AbstractionFunctionInfo(it)
            }
                ?: traitFinderContext.findTraitFunction(methodName, firstArgument)
        }?.let { sequenceOf(it) }
            ?: parent.findFullFunction(caller, name, numParams, firstArgument)

    override fun findPartialFunction(
        caller: String,
        name: String,
        numParams: Int,
        firstArgument: Type
    ): Sequence<FunctionInfo> {
        return sequenceOf(
            abstractions.findPartialFunction(name, numParams),
            traitFinderContext.findPartialTraitFunction(name, numParams, firstArgument),
            parent.findPartialFunction(caller, name, numParams, firstArgument)
        ).flatten()
    }

}

class ImplInferenceContext(
    private val parent: InferenceContext,
    private val traitType: TraitType,
    private val abstractions: AbstractionNodeMap
) : ObjectInferenceContext(traitType), CodeInferenceContext by AbstractionsCodeInferenceContext(abstractions) {

    override val traitFinderContext: TraitFinderContext
            by lazy { parent.traitFinderContext }

    override fun findFullFunction(
        caller: String,
        name: String,
        numParams: Int,
        firstArgument: Type
    ): Sequence<FunctionInfo>? =
        methodName(name, numParams).let { methodName ->
            val sameToCaller = methodName == caller
            run(sameToCaller) {
                abstractions[methodName]?.let {
                    AbstractionFunctionInfo(it)
                }
            }
                ?: traitType.methods[methodName]?.let {
                    methodTypeToFunctionInfo(traitType, it, checker)
                }
                ?: traitFinderContext.findTraitFunction(methodName, firstArgument)
        }?.let { sequenceOf(it) }
            ?: parent.findFullFunction(caller, name, numParams, firstArgument)

    override fun findPartialFunction(
        caller: String,
        name: String,
        numParams: Int,
        firstArgument: Type
    ): Sequence<FunctionInfo> {
        return sequenceOf(
            abstractions.findPartialFunction(name, numParams),
            "$name/".let {
                traitType.methods
                    .asSequence()
                    .filter { (name, methodType) ->
                        name.startsWith(name) && methodType.arguments.arity > numParams
                    }.map {
                        methodTypeToFunctionInfo(traitType, it.value, checker)
                    }
            },
            traitFinderContext.findPartialTraitFunction(name, numParams, firstArgument),
            parent.findPartialFunction(caller, name, numParams, firstArgument)
        ).flatten()
    }

}

class AbstractionFunctionInfo(val abstraction: AbstractionNode<AbstractionSemanticInfo>) : FunctionInfo {
    override val attributes: Set<Attribute> = abstraction.attributes
    override val name: String = abstraction.name
    override val arity: Int by lazy {
        abstraction.info.parameters.size
    }
    override val types: List<Type>
        get() {
            val info = abstraction.info
            return info.getInferredType(Location.NoProvided)
                .valueOrNull!!.cast<FunctionType>().arguments
        }
}

private fun List<AbstractionNode<AbstractionSemanticInfo>>.toMap() =
    associateBy {
        it.nameWithArity
    }.toMutableMap()

fun List<AbstractionNode<SemanticInfo>>.toInferenceContext(
    typeSystem: TypeSystem,
    impls: Set<Impl>,
) =
    SemanticModuleInfoInferenceContext(
        TraitFinderContext(typeSystem, impls.asSequence()),
        this.cast<List<AbstractionNode<AbstractionSemanticInfo>>>()
            .toMap()
    )

fun List<AbstractionNode<SemanticInfo>>.toTraitInferenceContext(
    inferenceContext: InferenceContext,
    traitType: TraitType
) =
    TraitInferenceContext(
        inferenceContext,
        traitType,
        this.cast<List<AbstractionNode<AbstractionSemanticInfo>>>()
            .toMap()
    )

fun List<AbstractionNode<SemanticInfo>>.toImplInferenceContext(
    inferenceContext: InferenceContext,
    traitType: TraitType
) =
    ImplInferenceContext(
        inferenceContext,
        traitType,
        this.cast<List<AbstractionNode<AbstractionSemanticInfo>>>()
            .toMap()
    )
