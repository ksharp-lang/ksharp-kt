package org.ksharp.nodes

import org.ksharp.common.Location
import org.ksharp.common.cast

data class ModuleNode(
    val name: String,
    val imports: Map<String, ImportNode>,
    val types: Map<String, TypeNode>,
    val typeDeclarations: Map<String, TypeDeclarationNode>,
    val functions: Map<String, FunctionNode>,
    override val location: Location
) : NodeData() {

    override val children: Sequence<NodeData>
        get() = sequenceOf(
            imports.values.asSequence(),
            types.values.asSequence(),
            typeDeclarations.values.asSequence(),
            functions.values.asSequence()
        ).flatten().cast()

}