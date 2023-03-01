package org.ksharp.nodes

import org.ksharp.common.Location
import org.ksharp.common.cast

data class ModuleNode(
    val name: String,
    val imports: List<ImportNode>,
    val types: List<NodeData>,
    val typeDeclarations: List<TypeDeclarationNode>,
    val functions: List<FunctionNode>,
    override val location: Location
) : NodeData() {

    override val children: Sequence<NodeData>
        get() = sequenceOf(
            imports.asSequence(),
            types.asSequence(),
            typeDeclarations.asSequence(),
            functions.asSequence()
        ).flatten().cast()

}