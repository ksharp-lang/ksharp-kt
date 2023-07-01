package org.ksharp.ir

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.ir.truffle.collections.ListNode
import org.ksharp.ir.truffle.collections.MapNode
import org.ksharp.ir.truffle.collections.SetNode
import org.ksharp.typesystem.attributes.Attribute

sealed interface IrCollections : IrExpression

data class IrList(
    override val attributes: Set<Attribute>,
    val items: List<IrExpression>,
    override val location: Location
) : ListNode(items.cast()), IrCollections

data class IrSet(
    override val attributes: Set<Attribute>,
    val items: List<IrExpression>,
    override val location: Location
) : SetNode(items.cast()), IrCollections

data class IrMap(
    override val attributes: Set<Attribute>,
    val entries: List<IrPair>,
    override val location: Location
) : MapNode(entries), IrExpression
