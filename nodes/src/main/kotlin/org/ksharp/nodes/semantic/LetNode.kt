package org.ksharp.nodes.semantic

import org.ksharp.common.Location
import org.ksharp.common.Table
import org.ksharp.common.TableBuilder
import org.ksharp.common.TableValue
import org.ksharp.nodes.NodeData

data class LetSemanticInfo(
    val table: TableBuilder<Symbol>,
) : SemanticInfo(), SymbolResolver, Table<Symbol> {
    override fun getSymbol(name: String): Symbol? =
        table[name]?.first?.also { it.used.activate() }

    override fun get(name: String): TableValue<Symbol>? = table[name]
}

data class LetBindingNode<SemanticInfo>(
    val match: SemanticNode<SemanticInfo>,
    val expression: SemanticNode<SemanticInfo>,
    override val info: SemanticInfo,
    override val location: Location,
) : SemanticNode<SemanticInfo>() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(expression)

}

data class LetNode<SemanticInfo>(
    val bindings: List<LetBindingNode<SemanticInfo>>,
    val expression: SemanticNode<SemanticInfo>,
    override val info: SemanticInfo,
    override val location: Location
) : SemanticNode<SemanticInfo>() {
    override val children: Sequence<NodeData>
        get() = sequenceOf(bindings.asSequence(), sequenceOf(expression)).flatten()
}
