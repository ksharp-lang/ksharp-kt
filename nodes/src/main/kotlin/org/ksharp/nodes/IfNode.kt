package org.ksharp.nodes

import org.ksharp.common.Location

data class IfNodeLocations(
    val ifLocation: Location,
    val thenLocation: Location,
    val elseLocation: Location
) : NodeLocations

data class IfNode(
    val condition: NodeData,
    val trueExpression: NodeData,
    val falseExpression: NodeData,
    override val location: Location,
    override val locations: IfNodeLocations
) : NodeData(), ExpressionParserNode {
    override val children: Sequence<NodeData>
        get() = sequenceOf(condition, trueExpression, falseExpression)

}
