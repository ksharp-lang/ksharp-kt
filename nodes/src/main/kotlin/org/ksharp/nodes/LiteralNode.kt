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
    OperatorBinding
}

enum class LiteralCollectionType {
    List,
    Map,
    Set,
    Tuple
}

data class LiteralMapEntryNode(
    val key: NodeData,
    val value: NodeData,
    override val location: Location
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(key, value)
}

data class LiteralValueNode(
    val value: String,
    val type: LiteralValueType,
    override val location: Location
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = emptySequence()

}

data class LiteralCollectionNode(
    val values: List<NodeData>,
    val type: LiteralCollectionType,
    override val location: Location
) : NodeData() {
    override val children: Sequence<NodeData>
        get() = values.asSequence()

}

data class UnitNode(override val location: Location) : NodeData() {
    override val children: Sequence<NodeData>
        get() = emptySequence()
}