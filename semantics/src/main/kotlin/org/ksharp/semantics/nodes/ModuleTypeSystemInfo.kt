package org.ksharp.semantics.nodes

import org.ksharp.common.Error
import org.ksharp.module.Impl
import org.ksharp.nodes.ImplNode
import org.ksharp.semantics.inference.TraitFinderContext
import org.ksharp.typesystem.TypeSystem

data class ModuleTypeSystemInfo(
    val errors: List<Error>,
    val typeSystem: TypeSystem,
    val impls: Map<Impl, ImplNode>
) {

    val traitFinderContext: TraitFinderContext by lazy {
        object : TraitFinderContext {
            override val typeSystem: TypeSystem = this@ModuleTypeSystemInfo.typeSystem
            override val impls: Sequence<Impl> = this@ModuleTypeSystemInfo.impls.keys.asSequence()
        }
    }

}
