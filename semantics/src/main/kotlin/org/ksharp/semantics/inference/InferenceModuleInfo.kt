package org.ksharp.semantics.inference

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.module.FunctionInfo
import org.ksharp.module.ModuleInfo
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.semantics.nodes.AbstractionSemanticInfo
import org.ksharp.semantics.nodes.SemanticInfo
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.types.FunctionType
import org.ksharp.typesystem.types.Type

sealed interface InferenceModuleInfo {

    val typeSystem: TypeSystem

    fun findFunction(name: String, numParams: Int): FunctionInfo?

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

class ConcreteModuleInfo(private val moduleInfo: ModuleInfo) :
    InferenceModuleInfo {

    override val typeSystem: TypeSystem = moduleInfo.typeSystem

    override fun findFunction(name: String, numParams: Int): FunctionInfo? =
        moduleInfo.functions["$name/$numParams"]
}

class SemanticModuleInfo(
    override val typeSystem: TypeSystem,
    private val abstractions: Map<String, AbstractionNode<AbstractionSemanticInfo>>
) : InferenceModuleInfo {
    override fun findFunction(name: String, numParams: Int): FunctionInfo? =
        abstractions["$name/$numParams"]?.let {
            AbstractionFunctionInfo(it)
        }
}

fun List<AbstractionNode<SemanticInfo>>.toSemanticModuleInfo(typeSystem: TypeSystem) =
    SemanticModuleInfo(
        typeSystem,
        this.cast<List<AbstractionNode<AbstractionSemanticInfo>>>()
            .associateBy {
                "${it.name}/${it.info.parameters.size + 1}"
            }
    )
