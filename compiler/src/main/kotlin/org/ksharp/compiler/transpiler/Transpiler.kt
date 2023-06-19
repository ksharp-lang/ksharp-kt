package org.ksharp.compiler.transpiler

import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.semantics.nodes.AbstractionSemanticInfo
import org.ksharp.semantics.nodes.SemanticModuleInfo

interface Transpiler {
    val supportParametricAbstractions: Boolean get() = false

    fun transpile(abstractionNode: AbstractionNode<AbstractionSemanticInfo>)
}


fun SemanticModuleInfo.transpile(transpiler: Transpiler): String = TODO("No implemented yet")
