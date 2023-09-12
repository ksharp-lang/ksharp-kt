package org.ksharp.nodes

import org.ksharp.common.Error
import org.ksharp.common.Location
import org.ksharp.common.cast

data class ModuleNode(
    val name: String,
    val imports: List<ImportNode>,
    val types: List<TypeNode>,
    val traits: List<TraitNode>,
    val impls: List<ImplNode>,
    val typeDeclarations: List<TypeDeclarationNode>,
    val functions: List<FunctionNode>,
    val errors: List<Error>,
    override val location: Location
) : NodeData() {

    override val locations: NodeLocations
        get() = NoLocationsDefined

    override val children: Sequence<NodeData>
        get() = sequenceOf(
            imports.asSequence(),
            types.asSequence(),
            traits.asSequence(),
            impls.asSequence(),
            typeDeclarations.asSequence(),
            functions.asSequence()
        ).flatten().cast()

}
