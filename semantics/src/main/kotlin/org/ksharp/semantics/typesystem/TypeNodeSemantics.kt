package org.ksharp.semantics.typesystem

import org.ksharp.nodes.ConcreteTypeNode
import org.ksharp.nodes.ModuleNode
import org.ksharp.nodes.NodeData
import org.ksharp.nodes.TypeNode
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.prelude.types.preludeTypeSystem
import org.ksharp.semantics.scopes.ModuleSemanticNode
import org.ksharp.semantics.scopes.Table
import org.ksharp.typesystem.PartialTypeSystem
import org.ksharp.typesystem.TypeItemBuilder
import org.ksharp.typesystem.TypeSystemBuilder
import org.ksharp.typesystem.typeSystem
import org.ksharp.typesystem.types.alias
import org.ksharp.typesystem.types.type

private fun TypeItemBuilder.register(node: NodeData) =
    when (node) {
        is ConcreteTypeNode -> type(node.name)
        else -> TODO()
    }

private fun TypeSystemBuilder.register(node: TypeNode) =
    when (node.expr) {
        is ConcreteTypeNode -> {
            alias(node.name, listOf()) {
                this.register(node.expr)
            }
        }

        else -> TODO()
    }

private fun TypeNode.checkSemantics(
    table: TypeVisibilityTableBuilder,
    builder: TypeSystemBuilder
) = table.register(
    name,
    if (internal) TypeVisibility.Internal else TypeVisibility.Public,
    location
).map {
    builder.register(this)
}

private fun List<TypeNode>.checkSemantics(errors: ErrorCollector): Pair<Table<TypeVisibility>, PartialTypeSystem> {
    val table = TypeVisibilityTableBuilder(errors)
    val typeSystem = typeSystem(preludeTypeSystem) {
        this@checkSemantics.map {
            it.checkSemantics(table, this)
        }
    }
    return table.build() to typeSystem
}

fun ModuleNode.checkSemantics(): ModuleSemanticNode {
    val errors = ErrorCollector()
    val (typeTable, typeSystem) = types.checkSemantics(errors)
    errors.collectAll(typeSystem.errors)
    return ModuleSemanticNode(
        errors.build(),
        typeTable,
        typeSystem.value
    )
}
