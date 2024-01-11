package org.ksharp.ir

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.ir.serializer.IrNodeSerializers
import org.ksharp.ir.truffle.relational.GeNode
import org.ksharp.ir.truffle.relational.GtNode
import org.ksharp.ir.truffle.relational.LeNode
import org.ksharp.ir.truffle.relational.LtNode
import org.ksharp.typesystem.attributes.Attribute

data class IrLt(
    override val attributes: Set<Attribute>,
    override val left: IrExpression,
    override val right: IrExpression,
    override val location: Location
) : LtNode(left.cast(), right.cast()), IrBinaryOperation {
    override val serializer: IrNodeSerializers
        get() = IrNodeSerializers.Lt

}

data class IrLe(
    override val attributes: Set<Attribute>,
    override val left: IrExpression,
    override val right: IrExpression,
    override val location: Location
) : LeNode(left.cast(), right.cast()), IrBinaryOperation {
    override val serializer: IrNodeSerializers
        get() = IrNodeSerializers.Le

}

data class IrGe(
    override val attributes: Set<Attribute>,
    override val left: IrExpression,
    override val right: IrExpression,
    override val location: Location
) : GeNode(left.cast(), right.cast()), IrBinaryOperation {
    override val serializer: IrNodeSerializers
        get() = IrNodeSerializers.Ge

}

data class IrGt(
    override val attributes: Set<Attribute>,
    override val left: IrExpression,
    override val right: IrExpression,
    override val location: Location
) : GtNode(left.cast(), right.cast()), IrBinaryOperation {
    override val serializer: IrNodeSerializers
        get() = IrNodeSerializers.Gt

}
