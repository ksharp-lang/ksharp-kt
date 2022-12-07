package ksharp.nodes

import org.ksharp.typesystem.types.Type

@Suppress("UNCHECKED_CAST")
fun <T> Node.cast(): T = this as T

sealed class Node {
    abstract val parent: Node
    abstract val children: List<Node>
    abstract val type: Type
}

