package org.ksharp.nodes

import org.ksharp.common.Location

data class IfNode(
    val condition: NodeData,
    val trueExpression: NodeData,
    val falseExpression: NodeData,
    override val location: Location
) : NodeData(), ExpressionParserNode {
    override val children: Sequence<NodeData>
        get() = sequenceOf(condition, trueExpression, falseExpression)

}