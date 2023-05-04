package org.ksharp.semantics.nodes

import org.ksharp.common.Flag

data class Symbol(val name: String, val type: TypePromise) : SemanticInfo() {
    val used: Flag = Flag()
}
