package org.ksharp.nodes

import org.ksharp.common.Location

data class Node(
    val parent: Node?,
    val location: Location,
    private val content: NodeData,
) {
    val children: Sequence<Node> = content.children(this)

    @Suppress("UNCHECKED_CAST")
    fun <T> cast(): T = content as T
}

abstract class NodeData {
    abstract val locations: NodeLocations
    abstract val location: Location
    protected abstract val children: Sequence<NodeData>
    val node: Node get() = Node(null, location, this)
    fun children(parent: Node): Sequence<Node> = this.children.map {
        Node(parent, location, it)
    }
}

sealed interface ExpressionParserNode

sealed interface NodeLocations

object NoLocationsDefined : NodeLocations
