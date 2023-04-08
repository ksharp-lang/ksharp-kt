package org.ksharp.semantics.nodes

import org.ksharp.common.Flag
import org.ksharp.semantics.inference.TypePromise

data class Symbol(val name: String, val type: TypePromise) : SemanticInfo() {
    val used: Flag = Flag()
}
