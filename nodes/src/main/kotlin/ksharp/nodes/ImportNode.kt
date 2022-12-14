package ksharp.nodes

data class ImportNode(
    val moduleName: String,
    val key: String
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = emptySequence()

}