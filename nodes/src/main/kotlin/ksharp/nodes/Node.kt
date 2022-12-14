package ksharp.nodes

class Node(
    val parent: Node?,
    private val content: NodeData,
) {
    val children: Sequence<Node> = content.children(this)

    @Suppress("UNCHECKED_CAST")
    fun <T> cast(): T = content as T
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Node

        if (parent != other.parent) return false
        if (content != other.content) return false

        return true
    }

    override fun hashCode(): Int {
        var result = parent?.hashCode() ?: 0
        result = 31 * result + content.hashCode()
        return result
    }

}

sealed class NodeData {
    protected abstract val children: Sequence<NodeData>
    val node: Node get() = Node(null, this)
    fun children(parent: Node): Sequence<Node> = this.children.map {
        Node(parent, it)
    }
}