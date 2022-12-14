package ksharp.nodes

data class ModuleNode(
    val name: String,
    val imports: List<ImportNode>
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = imports.asSequence()

}