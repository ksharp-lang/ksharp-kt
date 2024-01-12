package org.ksharp.ir

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.ir.serializer.IrNodeSerializers
import org.ksharp.ir.truffle.bitwise.*
import org.ksharp.typesystem.attributes.Attribute

data class IrBitAnd(
    override val attributes: Set<Attribute>,
    override val left: IrExpression,
    override val right: IrExpression,
    override val location: Location
) : BitAndNode(left.cast(), right.cast()), IrBinaryOperation {
    override val serializer: IrNodeSerializers
        get() = IrNodeSerializers.BitAnd

}

data class IrBitOr(
    override val attributes: Set<Attribute>,
    override val left: IrExpression,
    override val right: IrExpression,
    override val location: Location
) : BitOrNode(left.cast(), right.cast()), IrBinaryOperation {
    override val serializer: IrNodeSerializers
        get() = IrNodeSerializers.BitOr

}

data class IrBitXor(
    override val attributes: Set<Attribute>,
    override val left: IrExpression,
    override val right: IrExpression,
    override val location: Location
) : BitXorNode(left.cast(), right.cast()), IrBinaryOperation {
    override val serializer: IrNodeSerializers
        get() = IrNodeSerializers.BitXor

}

data class IrBitShr(
    override val attributes: Set<Attribute>,
    override val left: IrExpression,
    override val right: IrExpression,
    override val location: Location
) : BitShrNode(left.cast(), right.cast()), IrBinaryOperation {
    override val serializer: IrNodeSerializers
        get() = IrNodeSerializers.BitShr

}

data class IrBitShl(
    override val attributes: Set<Attribute>,
    override val left: IrExpression,
    override val right: IrExpression,
    override val location: Location
) : BitShlNode(left.cast(), right.cast()), IrBinaryOperation {
    override val serializer: IrNodeSerializers
        get() = IrNodeSerializers.BitShl

}
