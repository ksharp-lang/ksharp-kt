package org.ksharp.nodes

import org.ksharp.common.Location

enum class LiteralValueType {
    Character,
    String,
    MultiLineString,
    Integer,
    HexInteger,
    BinaryInteger,
    OctalInteger,
    Decimal,
    Binding,
    Label,
    OperatorBinding
}

enum class LiteralCollectionType {
    List,
    Map,
    Set,
    Tuple
}

data class LiteralCollectionNodeLocations(
    val openLocation: Location,
    val closeLocation: Location
) : NodeLocations

data class LiteralMapEntryNodeLocations(
    val keyValueOperatorLocation: Location
) : NodeLocations

data class LiteralMapEntryNode(
    val key: NodeData,
    val value: NodeData,
    override val location: Location,
    override val locations: LiteralMapEntryNodeLocations
) : NodeData(), ExpressionParserNode {
    override val children: Sequence<NodeData>
        get() = sequenceOf(key, value)
}

data class LiteralValueNode(
    val value: String,
    val type: LiteralValueType,
    override val location: Location
) : NodeData(), ExpressionParserNode {
    override val children: Sequence<NodeData>
        get() = emptySequence()

    override val locations: NodeLocations
        get() = NoLocationsDefined

}

data class LiteralCollectionNode(
    val values: List<NodeData>,
    val type: LiteralCollectionType,
    override val location: Location,
    override val locations: LiteralCollectionNodeLocations
) : NodeData(), ExpressionParserNode {
    override val children: Sequence<NodeData>
        get() = values.asSequence()

}

data class UnitNode(override val location: Location) : NodeData(), ExpressionParserNode {
    override val children: Sequence<NodeData>
        get() = emptySequence()

    override val locations: NodeLocations
        get() = NoLocationsDefined
}
