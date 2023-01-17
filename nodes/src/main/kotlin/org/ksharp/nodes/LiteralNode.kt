package org.ksharp.nodes

import org.ksharp.common.Location

enum class LiteralValueType {
    Character,
    String,
    MultiString,
    Integer,
    HexInteger,
    BinaryInteger,
    OctalInteger,
    Decimal
}

enum class LiteralCollectionType {
    List,
    Map,
    Set
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