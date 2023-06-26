package org.ksharp.ir

import org.ksharp.common.Location
import org.ksharp.typesystem.attributes.Attribute

sealed interface IrCollections : IrExpression

data class IrList(
    override val attributes: Set<Attribute>,
    val items: List<IrExpression>,
    override val location: Location
) : IrCollections

data class IrSet(
    override val attributes: Set<Attribute>,
    val items: List<IrExpression>,
    override val location: Location
) : IrCollections

data class IrMap(
    override val attributes: Set<Attribute>,
    val entries: List<IrPair>,
    override val location: Location
) : IrExpression
