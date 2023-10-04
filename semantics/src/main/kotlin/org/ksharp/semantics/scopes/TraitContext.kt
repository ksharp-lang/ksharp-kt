package org.ksharp.semantics.scopes

import org.ksharp.module.TraitInfo
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.semantics.nodes.SemanticInfo
import org.ksharp.typesystem.types.TraitType

interface TraitContext {

    val definitionMethods: Sequence<String>

    fun isMethodImplemented(name: String): Boolean

}

class TraitInfoTaitContext(private val info: TraitInfo) : TraitContext {
    override val definitionMethods: Sequence<String>
        get() = info.definitions.keys.asSequence()

    override fun isMethodImplemented(name: String): Boolean =
        info.implementations.contains(name)

}

class TraitTypeTraitContext(
    private val type: TraitType,
    private val abstractions: List<AbstractionNode<SemanticInfo>>
) : TraitContext {

    private val impls: Set<String> = abstractions.asSequence().map {
        it.name
    }.toSet();

    override val definitionMethods: Sequence<String>
        get() = type.methods.keys.asSequence()

    override fun isMethodImplemented(name: String): Boolean =
        impls.contains(name)
}
