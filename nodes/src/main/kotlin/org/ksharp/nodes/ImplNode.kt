package org.ksharp.nodes

import org.ksharp.common.Location

data class ImplNodeLocations(
    val traitName: Location,
    val forKeyword: Location,
    val assignOperator: Location,
) : NodeLocations

data class ImplNode(
    val traitName: String,
    val forType: TypeExpression,
    val functions: List<FunctionNode>,
    override val location: Location,
    override val locations: ImplNodeLocations
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = functions.asSequence()

}
