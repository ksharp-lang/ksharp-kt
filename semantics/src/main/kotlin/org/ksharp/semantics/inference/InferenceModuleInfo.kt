package org.ksharp.semantics.inference

import org.ksharp.common.Either
import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.module.FunctionInfo
import org.ksharp.module.ModuleInfo
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.semantics.nodes.AbstractionSemanticInfo
import org.ksharp.semantics.nodes.SemanticInfo
import org.ksharp.semantics.nodes.getType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.types.FunctionType
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.types.newParameter

sealed interface InferenceModuleInfo {

    val isKernelModule: Boolean

    val typeSystem: TypeSystem

    fun findFunction(name: String, numParams: Int): Sequence<FunctionInfo>?

}

class AbstractionFunctionInfo(val abstraction: AbstractionNode<AbstractionSemanticInfo>) : FunctionInfo {
    override val attributes: Set<Attribute> = abstraction.attributes
    override val name: String = abstraction.name
    override val types: List<Type>
        get() {
            if (abstraction.info.hasInferredType()) {
                return abstraction.info.getInferredType(Location.NoProvided)
                    .valueOrNull!!.cast<FunctionType>().arguments
            }
            val arguments = abstraction.info.parameters.map { i ->
                when (val iType = i.getInferredType(abstraction.location)) {
                    is Either.Right -> iType.value
                    else -> newParameter()
                }
            }
            val returnType = abstraction.info.returnType?.getType(abstraction.location)?.valueOrNull
            return if (returnType != null) arguments + returnType else arguments
        }
}

class ConcreteModuleInfo(private val moduleInfo: ModuleInfo, override val isKernelModule: Boolean) :
    InferenceModuleInfo {

    override val typeSystem: TypeSystem = moduleInfo.typeSystem

    override fun findFunction(name: String, numParams: Int): Sequence<FunctionInfo>? =
        moduleInfo.functions[name]?.let { fns ->
            fns.asSequence().filter { it.types.size == numParams }
        }

}

class SemanticModuleInfo(
    override val typeSystem: TypeSystem,
    private val abstractions: Map<String, List<AbstractionNode<AbstractionSemanticInfo>>>
) : InferenceModuleInfo {

    override val isKernelModule: Boolean = false
    override fun findFunction(name: String, numParams: Int): Sequence<FunctionInfo>? =
        abstractions[name]?.let { fns ->
            fns.asSequence().filter { (it.info.parameters.size + 1) == numParams }
                .map {
                    AbstractionFunctionInfo(it)
                }
        }

}

fun List<AbstractionNode<SemanticInfo>>.toSemanticModuleInfo(typeSystem: TypeSystem) =
    SemanticModuleInfo(
        typeSystem,
        this.cast<List<AbstractionNode<AbstractionSemanticInfo>>>().groupBy { it.name }
    )
