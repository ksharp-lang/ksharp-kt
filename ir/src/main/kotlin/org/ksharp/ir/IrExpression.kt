package org.ksharp.ir

import com.oracle.truffle.api.CallTarget
import com.oracle.truffle.api.nodes.RootNode
import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.ir.serializer.IrNodeSerializers
import org.ksharp.ir.truffle.ArgAccessNode
import org.ksharp.ir.truffle.IfNode
import org.ksharp.ir.truffle.KSharpNode
import org.ksharp.ir.truffle.call.CallNode
import org.ksharp.ir.truffle.cast.NumCastNode
import org.ksharp.ir.truffle.variable.VarAccessNode
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.types.Type

sealed interface IrExpression : IrSymbol

sealed interface IrBinaryOperation : IrExpression {
    val left: IrExpression
    val right: IrExpression
}

enum class CastType {
    Byte,
    Short,
    Int,
    Long,
    Float,
    Double,
    BigInt,
    BigDecimal,
}

data class IrNumCast(
    val expr: IrExpression,
    val type: CastType,
    override val location: Location
) : NumCastNode(type, expr.cast()), IrExpression {
    override val attributes: Set<Attribute>
        get() = NoAttributes

    override val serializer: IrNodeSerializers = IrNodeSerializers.NumCast
}

data class IrPair(
    override val attributes: Set<Attribute>,
    val first: IrExpression,
    val second: IrExpression,
    override val location: Location
) : IrExpression {
    override val serializer: IrNodeSerializers = IrNodeSerializers.Pair
}

data class IrArg(
    override val attributes: Set<Attribute>,
    val index: Int,
    override val location: Location
) : ArgAccessNode(index), IrExpression {
    override val serializer: IrNodeSerializers = IrNodeSerializers.NoDefined
}

data class IrVar(
    override val attributes: Set<Attribute>,
    val index: Int,
    override val location: Location
) : VarAccessNode(index), IrExpression {
    override val serializer: IrNodeSerializers = IrNodeSerializers.NoDefined
}


data class IrIf(
    override val attributes: Set<Attribute>,
    val condition: IrExpression,
    val thenExpr: IrExpression,
    val elseExpr: IrExpression,
    override val location: Location
) : IfNode(condition.cast(), thenExpr.cast(), elseExpr.cast()), IrExpression {
    override val serializer: IrNodeSerializers = IrNodeSerializers.NoDefined
}

data class IrCall(
    override val attributes: Set<Attribute>,
    val module: String?,
    val function: String,
    val arguments: List<IrExpression>,
    val type: Type,
    override val location: Location
) : CallNode(arguments.cast<List<KSharpNode>>().toTypedArray()), IrExpression {

    lateinit var functionLookup: FunctionLookup

    override fun getCallTarget(): CallTarget? =
        functionLookup.find(module, function, type)?.cast<RootNode>()?.callTarget

    override val serializer: IrNodeSerializers = IrNodeSerializers.NoDefined

}
