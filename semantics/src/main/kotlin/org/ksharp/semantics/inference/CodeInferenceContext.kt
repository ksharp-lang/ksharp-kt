package org.ksharp.semantics.inference

import org.ksharp.common.cast
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.nodes.semantic.SemanticInfo


sealed interface CodeInferenceContext {
    /**
     * Register partial function abstraction
     * @param previousName the name what the abstraction was called
     */
    fun registerPartialFunctionAbstraction(previousName: String, abstraction: AbstractionNode<SemanticInfo>)

    fun resolvePartialFunctions(caller: String, name: String, info: InferenceInfo)
}

class AbstractionsCodeInferenceContext(
    private val abstractions: AbstractionNodeMap
) : CodeInferenceContext {
    override fun registerPartialFunctionAbstraction(previousName: String, abstraction: AbstractionNode<SemanticInfo>) {
        abstractions.remove(previousName)
        abstractions[abstraction.nameWithArity] = abstraction.cast()
    }

    override fun resolvePartialFunctions(caller: String, name: String, info: InferenceInfo) {
        "$name/".let { prefixName ->
            abstractions
                .asSequence()
                .filter { (key, _) ->
                    key.startsWith(prefixName)
                }.forEach {
                    it.value.cast<AbstractionNode<SemanticInfo>>()
                        .inferType(caller, info)
                }
        }
    }
}
