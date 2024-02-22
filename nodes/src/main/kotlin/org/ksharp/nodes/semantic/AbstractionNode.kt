package org.ksharp.nodes.semantic

import org.ksharp.common.Either
import org.ksharp.common.Location
import org.ksharp.nodes.NodeData
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.types.PartialFunctionType

data class AbstractionSemanticInfo(
    private var _parameters: List<SemanticInfo>,
    val returnType: TypePromise? = null
) : SemanticInfo() {

    val parameters: List<SemanticInfo>
        get() = _parameters

    fun updateParameters(partial: PartialFunctionType) {
        if (_parameters.isEmpty()) {
            _parameters = partial.arguments.dropLast(1).map {
                TypeSemanticInfo(Either.Right(it))
            }
        }
    }

}

data class AbstractionNode<SemanticInfo>(
    val attributes: Set<Attribute>,
    val name: String,
    val expression: SemanticNode<SemanticInfo>,
    override val info: SemanticInfo,
    override val location: Location
) : SemanticNode<SemanticInfo>() {

    override val children: Sequence<NodeData>
        get() = sequenceOf(expression)

}

data class AbstractionLambdaNode<SemanticInfo>(
    val expression: SemanticNode<SemanticInfo>,
    override val info: SemanticInfo,
    override val location: Location
) : SemanticNode<SemanticInfo>() {

    override val children: Sequence<NodeData>
        get() = sequenceOf(expression)

}
