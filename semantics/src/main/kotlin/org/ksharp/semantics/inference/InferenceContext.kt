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
import org.ksharp.typesystem.unification.UnificationChecker
import org.ksharp.typesystem.unification.unify

typealias AbstractionNodeMap = Map<String, AbstractionNode<AbstractionSemanticInfo>>

enum class FindFunctionMode {
    Partial,
    Complete
}

private inline fun run(sameToCaller: Boolean, action: () -> FunctionInfo?): FunctionInfo? =
    if (sameToCaller) null else action()

sealed class InferenceContext : TraitFinderContext {
    val checker: UnificationChecker by lazy { unificationChecker(this) }
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

class ModuleInfoInferenceContext(private val moduleInfo: ModuleInfo) :
    InferenceContext() {

    override val typeSystem: TypeSystem = moduleInfo.typeSystem

    override val impls: Sequence<Impl> = moduleInfo.impls.asSequence()

    override fun findFullFunction(
        caller: String,
        name: String,
        numParams: Int,
        firstArgument: Type
    ): Sequence<FunctionInfo>? =
        methodName(name, numParams).let { methodName ->
            moduleInfo.functions[methodName] ?: findTraitFunction(methodName, firstArgument)
        }?.let { sequenceOf(it) }

    override fun findPartialFunction(
        caller: String,
        name: String,
        numParams: Int,
        firstArgument: Type
    ): Sequence<FunctionInfo> {
        return "$name/".let { prefixName ->
            moduleInfo.functions
                .asSequence()
                .filter { (key, value) ->
                    key.startsWith(prefixName) && value.arity > numParams
                }.map { it.value }
            //TODO join with partial traits to calculate the partial trait functions
        }
    }

    override fun unify(name: String, location: Location, type: ErrorOrType): ErrorOrType = type
}

class SemanticModuleInfoInferenceContext(
    override val typeSystem: TypeSystem,
    override val impls: Sequence<Impl>,
    private val abstractions: AbstractionNodeMap
) : InferenceContext() {
    override fun findFullFunction(
        caller: String,
        name: String,
        numParams: Int,
        firstArgument: Type
    ): Sequence<FunctionInfo>? =
        methodName(name, numParams).let { methodName ->
            abstractions[methodName]?.let {
                AbstractionFunctionInfo(it)
            } ?: findTraitFunction(methodName, firstArgument)
        }?.let { sequenceOf(it) }

    override fun unify(name: String, location: Location, type: ErrorOrType): ErrorOrType = type

    override fun findPartialFunction(
        caller: String,
        name: String,
        numParams: Int,
        firstArgument: Type
    ): Sequence<FunctionInfo> {
        return "$name/".let { prefixName ->
            abstractions
                .asSequence()
                .filter { (key, value) ->
                    key.startsWith(prefixName) && value.info.parameters.size > numParams
                }.map {
                    AbstractionFunctionInfo(it.value)
                }
            //TODO join with partial traits to calculate the partial trait functions
        }
    }
}

class TraitInferenceContext(
    private val parent: InferenceContext,
    private val traitType: TraitType,
    private val abstractions: AbstractionNodeMap
) : InferenceContext() {

    override val impls: Sequence<Impl>
        get() = parent.impls

    override val typeSystem: TypeSystem
        get() = parent.typeSystem

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
                ?: findTraitFunction(methodName, firstArgument)
        }?.let { sequenceOf(it) }
            ?: parent.findFullFunction(caller, name, numParams, firstArgument)

    override fun unify(name: String, location: Location, type: ErrorOrType): ErrorOrType =
        type.flatMap { t ->
            traitType.methods[name]?.unify(location, t, checker) ?: type
        }

    override fun findPartialFunction(
        caller: String,
        name: String,
        numParams: Int,
        firstArgument: Type
    ): Sequence<FunctionInfo> {
        //TODO: implement
        return emptySequence()
    }
}

class ImplInferenceContext(
    private val parent: InferenceContext,
    private val traitType: TraitType,
    private val abstractions: AbstractionNodeMap
) : InferenceContext() {

    override val impls: Sequence<Impl> = parent.impls

    override val typeSystem: TypeSystem
        get() = parent.typeSystem

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
                ?: findTraitFunction(methodName, firstArgument)
        }?.let { sequenceOf(it) }
            ?: parent.findFullFunction(caller, name, numParams, firstArgument)

    override fun unify(name: String, location: Location, type: ErrorOrType): ErrorOrType =
        type.flatMap { t ->
            traitType.methods[name]?.unify(location, t, checker) ?: type
        }

    override fun findPartialFunction(
        caller: String,
        name: String,
        numParams: Int,
        firstArgument: Type
    ): Sequence<FunctionInfo> {
        //TODO: implement
        return emptySequence()
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
    }

fun List<AbstractionNode<SemanticInfo>>.toInferenceContext(
    typeSystem: TypeSystem,
    impls: Set<Impl>,
) =
    SemanticModuleInfoInferenceContext(
        typeSystem,
        impls.asSequence(),
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
