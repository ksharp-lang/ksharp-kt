package org.ksharp.ir

import org.ksharp.typesystem.attributes.Attribute

val NoCaptured: String? = null

enum class VarKind {
    Var,
    Arg
}

data class VarInfo(
    val index: Int,
    val kind: VarKind,
    val attributes: Set<Attribute>,
    val captureName: String? = NoCaptured
)

interface VariableIndex {
    val size: Int
    operator fun get(name: String): VarInfo?
}

class ClosureIndex(val arguments: VariableIndex, val context: VariableIndex) : VariableIndex {
    override val size: Int
        get() = arguments.size + 1

    val captured = mutableMapOf<String, VarInfo>()

    override operator fun get(name: String): VarInfo? {
        val arg = arguments[name]
        if (arg != null) return arg

        val alreadyCaptured = captured[name]
        if (alreadyCaptured != null) return alreadyCaptured.copy(captureName = name)

        val captureVar = context[name]
        if (captureVar != null) {
            captured[name] = captureVar.copy(captureName = name)
        }

        return captured[name]
    }
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

fun closureIndex(index: VariableIndex, parent: VariableIndex) = ClosureIndex(index, parent)

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
