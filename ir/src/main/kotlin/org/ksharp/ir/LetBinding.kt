package org.ksharp.ir

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.ir.serializer.IrNodeSerializers
import org.ksharp.ir.truffle.KSharpNode
import org.ksharp.ir.truffle.variable.LetNode
import org.ksharp.ir.truffle.variable.SetVarNode
import org.ksharp.typesystem.attributes.Attribute

data class IrLet(
    override val attributes: Set<Attribute>,
    val expressions: List<IrExpression>,
    override val location: Location
) : LetNode(expressions.cast<List<KSharpNode>>().toTypedArray()), IrExpression {
    override val serializer: IrNodeSerializers = IrNodeSerializers.NoDefined
}

data class IrSetVar(
    override val attributes: Set<Attribute>,
    val index: Int,
    val value: IrExpression,
    override val location: Location
) : SetVarNode(index, value.cast()), IrExpression {
    override val serializer: IrNodeSerializers = IrNodeSerializers.NoDefined
}
