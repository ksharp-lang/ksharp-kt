package org.ksharp.semantics.inference

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.module.FunctionInfo
import org.ksharp.module.Impl
import org.ksharp.module.ModuleInfo
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.semantics.nodes.AbstractionSemanticInfo
import org.ksharp.semantics.nodes.SemanticInfo
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.types.FunctionType
import org.ksharp.typesystem.types.TraitType
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.unification.unify

typealias AbstractionNodeMap = Map<String, AbstractionNode<AbstractionSemanticInfo>>

private inline fun run(sameToCaller: Boolean, action: () -> FunctionInfo?): FunctionInfo? =
    if (sameToCaller) null else action()

private class TraitMethodTypeInfo(
    override val name: String,
    private val methodType: TraitType.MethodType
) : FunctionInfo {
    override val attributes: Set<Attribute> = methodType.attributes
    override val types: List<Type> = methodType.arguments
}

sealed interface InferenceContext {

    val typeSystem: TypeSystem

    val impls: Sequence<Impl>
    fun getTraitsImplemented(type: Type): Sequence<TraitType> = getTraitsImplemented(type, this)

    fun findFunction(caller: String, name: String, numParams: Int): FunctionInfo?
    fun unify(name: String, location: Location, type: ErrorOrType): ErrorOrType
    fun methodName(name: String, numParams: Int) = "$name/$numParams"

}

class ModuleInfoInferenceContext(private val moduleInfo: ModuleInfo) :
    InferenceContext {

    override val typeSystem: TypeSystem = moduleInfo.typeSystem

    override val impls: Sequence<Impl> = moduleInfo.impls.asSequence()

    override fun findFunction(caller: String, name: String, numParams: Int): FunctionInfo? =
        moduleInfo.functions["$name/$numParams"]

    override fun unify(name: String, location: Location, type: ErrorOrType): ErrorOrType = type
}

class SemanticModuleInfoInferenceContext(
    override val typeSystem: TypeSystem,
    override val impls: Sequence<Impl>,
    private val abstractions: AbstractionNodeMap
) : InferenceContext {
    override fun findFunction(caller: String, name: String, numParams: Int): FunctionInfo? =
        abstractions[methodName(name, numParams)]?.let {
            AbstractionFunctionInfo(it)
        }

    override fun unify(name: String, location: Location, type: ErrorOrType): ErrorOrType = type

}

class TraitInferenceContext(
    private val parent: InferenceContext,
    private val traitType: TraitType,
    private val abstractions: AbstractionNodeMap
) : InferenceContext {

    override val impls: Sequence<Impl>
        get() = parent.impls

    override val typeSystem: TypeSystem
        get() = parent.typeSystem

    override fun findFunction(caller: String, name: String, numParams: Int): FunctionInfo? =
        abstractions[methodName(name, numParams)]?.let {
            AbstractionFunctionInfo(it)
        } ?: parent.findFunction(caller, name, numParams)

    override fun unify(name: String, location: Location, type: ErrorOrType): ErrorOrType =
        type.flatMap { t ->
            traitType.methods[name]?.unify(location, t) ?: type
        }

}

class ImplInferenceContext(
    private val parent: InferenceContext,
    private val traitType: TraitType,
    private val abstractions: AbstractionNodeMap
) : InferenceContext {

    override val impls: Sequence<Impl> = parent.impls

    override val typeSystem: TypeSystem
        get() = parent.typeSystem

    override fun findFunction(caller: String, name: String, numParams: Int): FunctionInfo? =
        methodName(name, numParams).let { methodName ->
            val sameToCaller = methodName == caller
            run(sameToCaller) {
                abstractions[methodName]?.let {
                    AbstractionFunctionInfo(it)
                }
            } ?: traitType.methods[methodName]?.let {
                TraitMethodTypeInfo(methodName, it)
            } ?: parent.findFunction(caller, name, numParams)
        }

    override fun unify(name: String, location: Location, type: ErrorOrType): ErrorOrType =
        type.flatMap { t ->
            traitType.methods[name]?.unify(location, t) ?: type
        }
}

class AbstractionFunctionInfo(val abstraction: AbstractionNode<AbstractionSemanticInfo>) : FunctionInfo {
    override val attributes: Set<Attribute> = abstraction.attributes
    override val name: String = abstraction.name
    override val types: List<Type>
        get() {
            val info = abstraction.info
            return info.getInferredType(Location.NoProvided)
                .valueOrNull!!.cast<FunctionType>().arguments
        }
}

private fun List<AbstractionNode<AbstractionSemanticInfo>>.toMap() =
    associateBy {
        "${it.name}/${it.info.parameters.size + 1}"
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
