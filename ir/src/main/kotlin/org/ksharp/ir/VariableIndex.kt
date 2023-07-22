package org.ksharp.ir

import org.ksharp.typesystem.attributes.Attribute

enum class VarKind {
    Var,
    Arg
}

data class VarInfo(
    val index: Int,
    val kind: VarKind,
    val attributes: Set<Attribute>
)

interface VariableIndex {
    val size: Int
    operator fun get(name: String): VarInfo?
}

interface MutableVariableIndex : VariableIndex {
    operator fun set(name: String, varInfo: VarInfo)
}

val emptyVariableIndex = object : VariableIndex {
    override val size: Int = 0

    override operator fun get(name: String): VarInfo? = null
}

fun argIndex(positions: Map<String, VarInfo>) = object : VariableIndex {
    override val size: Int = 0

    override operator fun get(name: String): VarInfo? = positions[name]
}

fun mutableVariableIndexes(parent: VariableIndex) = object : MutableVariableIndex {
    private val repo = mutableMapOf<String, VarInfo>()

    override fun set(name: String, varInfo: VarInfo) {
        if (!repo.containsKey(name)) {
            repo[name] = varInfo
        } else error("Variable $name already exists")
    }

    override val size: Int
        get() = repo.size + parent.size


    override fun get(name: String): VarInfo? = repo[name] ?: parent[name]

}
