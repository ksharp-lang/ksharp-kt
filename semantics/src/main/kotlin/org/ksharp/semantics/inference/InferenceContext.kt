package org.ksharp.semantics.inference

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.module.FunctionInfo
import org.ksharp.module.Impl
import org.ksharp.module.ModuleInfo
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.semantics.nodes.AbstractionSemanticInfo
import org.ksharp.semantics.nodes.SemanticInfo
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.types.FunctionType
import org.ksharp.typesystem.types.TraitType
import org.ksharp.typesystem.types.Type

typealias AbstractionNodeMap = Map<String, AbstractionNode<AbstractionSemanticInfo>>

sealed interface InferenceContext {

    val typeSystem: TypeSystem

    val impls: Sequence<Impl>

    val traits: Sequence<TraitType>

    fun findFunction(name: String, numParams: Int): FunctionInfo?

    fun getTraitsImplemented(type: Type): Sequence<TraitType> = getTraitsImplemented(type, this)

    fun methodName(name: String, numParams: Int) = "$name/$numParams"

}

class ModuleInfoInferenceContext(private val moduleInfo: ModuleInfo) :
    InferenceContext {

    override val typeSystem: TypeSystem = moduleInfo.typeSystem

    override val impls: Sequence<Impl> = moduleInfo.impls.asSequence()

    override val traits: Sequence<TraitType> = moduleInfo.traits.keys.asSequence().map {
        typeSystem[it].valueOrNull!!.cast()
    }

    override fun findFunction(name: String, numParams: Int): FunctionInfo? =
        moduleInfo.functions["$name/$numParams"]
}

class SemanticModuleInfoInferenceContext(
    override val typeSystem: TypeSystem,
    override val impls: Sequence<Impl>,
    override val traits: Sequence<TraitType>,
    private val abstractions: AbstractionNodeMap
) : InferenceContext {
    override fun findFunction(name: String, numParams: Int): FunctionInfo? =
        abstractions[methodName(name, numParams)]?.let {
            AbstractionFunctionInfo(it)
        }

}

class TraitInferenceContext(
    private val parent: InferenceContext,
    private val abstractions: AbstractionNodeMap
) : InferenceContext {

    override val impls: Sequence<Impl>
        get() = parent.impls

    override val typeSystem: TypeSystem
        get() = parent.typeSystem

    override val traits: Sequence<TraitType> = parent.traits

    override fun findFunction(name: String, numParams: Int): FunctionInfo? =
        abstractions[methodName(name, numParams)]?.let {
            AbstractionFunctionInfo(it)
        } ?: parent.findFunction(name, numParams)

}

class ImplInferenceContext(
    private val parent: InferenceContext,
    private val traitType: TraitType,
    private val traitAbstractions: AbstractionNodeMap,
    private val abstractions: AbstractionNodeMap
) : InferenceContext {

    override val impls: Sequence<Impl> = parent.impls

    override val traits: Sequence<TraitType> = parent.traits

    override val typeSystem: TypeSystem
        get() = parent.typeSystem

    override fun findFunction(name: String, numParams: Int): FunctionInfo? =
        methodName(name, numParams).let { methodName ->
            abstractions[methodName]?.let {
                AbstractionFunctionInfo(it)
            } ?: traitType.methods[methodName]?.let {
                traitAbstractions[methodName]?.let {
                    AbstractionFunctionInfo(it)
                }
            } ?: parent.findFunction(name, numParams)
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

fun List<AbstractionNode<SemanticInfo>>.toSemanticModuleInfo(
    typeSystem: TypeSystem,
    impls: Set<Impl>,
    traits: List<TraitType>
) =
    SemanticModuleInfoInferenceContext(
        typeSystem,
        impls.asSequence(),
        traits.asSequence(),
        this.cast<List<AbstractionNode<AbstractionSemanticInfo>>>()
            .toMap()
    )

fun List<AbstractionNode<SemanticInfo>>.toTraitSemanticModuleInfo(semanticInfo: InferenceContext) =
    TraitInferenceContext(
        semanticInfo,
        this.cast<List<AbstractionNode<AbstractionSemanticInfo>>>()
            .toMap()
    )
