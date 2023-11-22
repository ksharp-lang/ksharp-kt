package org.ksharp.ir

import org.ksharp.common.Location
import org.ksharp.ir.serializer.IrNodeSerializers
import org.ksharp.ir.truffle.literals.*
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.CommonAttribute

sealed interface Literal : IrExpression {
    override val attributes: Set<Attribute>
        get() = setOf(CommonAttribute.Constant)

}

data class IrInteger(
    val value: Long,
    override val location: Location
) : IntegerNode(value), Literal {
    override val serializer: IrNodeSerializers = IrNodeSerializers.NoDefined
}

data class IrDecimal(
    val value: Double,
    override val location: Location
) : DecimalNode(value), Literal {
    override val serializer: IrNodeSerializers = IrNodeSerializers.NoDefined
}

data class IrCharacter(
    val value: Char,
    override val location: Location
) : CharacterNode(value), Literal {
    override val serializer: IrNodeSerializers = IrNodeSerializers.NoDefined
}

data class IrString(
    val value: String,
    override val location: Location
) : StringNode(value), Literal {
    override val serializer: IrNodeSerializers = IrNodeSerializers.NoDefined
}

data class IrBool(
    val value: Boolean,
    override val location: Location
) : BooleanNode(value), Literal {
    override val serializer: IrNodeSerializers = IrNodeSerializers.NoDefined
}
