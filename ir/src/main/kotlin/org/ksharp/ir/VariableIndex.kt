package org.ksharp.ir

import org.ksharp.typesystem.attributes.Attribute

data class VarInfo(
    val index: Int,
    val attributes: Set<Attribute>
)

interface VariableIndex {
    val size: Int
    operator fun get(name: String): VarInfo?
}

val emptyVariableIndex = object : VariableIndex {
    override val size: Int = 0

    override operator fun get(name: String): VarInfo? = null
}

fun variableIndex(positions: Map<String, VarInfo>) = object : VariableIndex {
    override val size: Int = positions.size

    override operator fun get(name: String): VarInfo? = positions[name]

}

fun chainVariableIndexes(first: VariableIndex, second: VariableIndex) = object : VariableIndex {
    override val size: Int = first.size + second.size

    override operator fun get(name: String): VarInfo? =
        first[name] ?: second[name]

}
