package org.ksharp.ir

import com.oracle.truffle.api.CallTarget
import com.oracle.truffle.api.nodes.RootNode
import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.ir.serializer.IrNodeSerializers
import org.ksharp.ir.transform.nativeApplicationName
import org.ksharp.ir.transform.nativeModuleName
import org.ksharp.ir.truffle.ArgAccessNode
import org.ksharp.ir.truffle.IfNode
import org.ksharp.ir.truffle.KSharpNode
import org.ksharp.ir.truffle.call.CallNode
import org.ksharp.ir.truffle.call.LambdaCallNode
import org.ksharp.ir.truffle.call.ModuleCallNode
import org.ksharp.ir.truffle.call.NativeCallNode
import org.ksharp.ir.truffle.cast.NumCastNode
import org.ksharp.ir.truffle.cast.ToStringNode
import org.ksharp.ir.truffle.variable.VarAccessNode
import org.ksharp.module.Impl
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.types.*

class CallNotFound(exception: Exception) : RuntimeException(exception)

sealed interface IrExpression : IrSymbol

sealed interface IrBinaryOperation : IrExpression {
    val left: IrExpression
    val right: IrExpression
}

sealed interface IrValueAccess : IrExpression {
    val index: Int
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

data class CallScope(
    val callName: String,
    val traitName: String?,
    val traitScopeName: String?,
)

data class IrNumCast(
    val expr: IrExpression,
    val type: CastType,
    override val location: Location
) : NumCastNode(type, expr.cast()), IrExpression {
    override val attributes: Set<Attribute>
        get() = NoAttributes

    override val serializer: IrNodeSerializers = IrNodeSerializers.NumCast
}

data class IrToString(
    val expr: IrExpression,
    override val location: Location
) : ToStringNode(expr.cast()), IrExpression {
    override val attributes: Set<Attribute>
        get() = NoAttributes

    override val serializer: IrNodeSerializers = IrNodeSerializers.ToString
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
    override val index: Int,
    override val location: Location
) : ArgAccessNode(index), IrValueAccess {
    override val serializer: IrNodeSerializers = IrNodeSerializers.Arg
}

data class IrVar(
    override val attributes: Set<Attribute>,
    override val index: Int,
    override val location: Location
) : VarAccessNode(index), IrValueAccess {
    override val serializer: IrNodeSerializers = IrNodeSerializers.Var
}


data class IrIf(
    override val attributes: Set<Attribute>,
    val condition: IrExpression,
    val thenExpr: IrExpression,
    val elseExpr: IrExpression,
    override val location: Location
) : IfNode(condition.cast(), thenExpr.cast(), elseExpr.cast()), IrExpression {
    override val serializer: IrNodeSerializers = IrNodeSerializers.If
}

data class IrCall(
    override val attributes: Set<Attribute>,
    val module: String?,
    val scope: CallScope,
    val arguments: List<IrExpression>,
    val type: Type,
    override val location: Location
) : CallNode(arguments.cast<List<KSharpNode>>().toTypedArray(), type), IrExpression {

    lateinit var functionLookup: FunctionLookup

    override fun getCallTarget(firstArgument: Type?): CallTarget? =
        functionLookup.find(module, scope, firstArgument).cast<RootNode>().callTarget

    override val serializer: IrNodeSerializers = IrNodeSerializers.Call

}

data class IrNativeCall(
    val mAttributes: Set<Attribute>,
    val functionClass: String,
    val arguments: List<IrExpression>,
    val type: Type,
    override val location: Location
) : NativeCallNode(functionClass, arguments.cast<List<KSharpNode>>().toTypedArray(), type), IrExpression {

    override val attributes: Set<Attribute>
        get() = call.getAttributes(mAttributes)

    override val serializer: IrNodeSerializers = IrNodeSerializers.NativeCall

}

data class IrModuleCall internal constructor(
    override val attributes: Set<Attribute>,
    val moduleName: String,
    val functionName: String,
    val arguments: List<IrExpression>,
    val type: FunctionType,
    override val location: Location
) : ModuleCallNode(arguments.cast<List<KSharpNode>>().toTypedArray(), type), IrExpression {

    private lateinit var loaderFn: LoadIrModuleFn

    constructor(
        attributes: Set<Attribute>,
        loaderFn: LoadIrModuleFn,
        moduleName: String,
        functionName: String,
        arguments: List<IrExpression>,
        type: FunctionType,
        location: Location
    ) : this(attributes, moduleName, functionName, arguments, type, location) {
        this.loaderFn = loaderFn
    }

    override val serializer: IrNodeSerializers get() = IrNodeSerializers.ModuleCall

    private fun getNativeCall(functionClass: String): Call =
        try {
            Class.forName(functionClass).getConstructor().newInstance() as Call
        } catch (e: Exception) {
            throw CallNotFound(e)
        }

    private fun getTraitCall(module: IrModule): Call {
        val firstArgument = type.arguments.first()
        val (traitType, implFunction) = when (firstArgument) {
            is ImplType -> {
                val impl = firstArgument.impl
                val functions = module.implSymbols[Impl("", firstArgument.trait.name, impl)]

                firstArgument.trait to functions?.firstOrNull {
                    it.name == functionName
                }
            }

            is FixedTraitType -> {
                firstArgument.trait to null
            }

            else -> firstArgument.cast<TraitType>() to null
        }
        val function = implFunction
            ?: module.traitSymbols[traitType.name]?.firstOrNull {
                it.name == functionName
            }
        if (function != null) {
            return FunctionCall(function.cast())
        }
        return getNativeCall(
            "${nativeModuleName(moduleName)}.impls.${traitType.name}For${firstArgument.representation}${
                functionName.replace(
                    "/",
                    ""
                ).replaceFirstChar { it.uppercaseChar() }
            }"
        )
    }

    private fun getCall(module: IrModule): Call {
        val function = module.symbols.firstOrNull {
            it.name == functionName
        }
        if (function != null) {
            return FunctionCall(function.cast())
        }
        return getNativeCall(nativeApplicationName(moduleName, functionName))
    }

    override fun getCall(): Call =
        loaderFn.load(moduleName)?.let {
            if (type.attributes.contains(CommonAttribute.TraitMethod)) {
                getTraitCall(it.irModule)
            } else getCall(it.irModule)
        }!!
}

data class IrLambdaCall(
    override val attributes: Set<Attribute>,
    val lambda: IrExpression,
    val arguments: List<IrExpression>,
    val type: Type,
    override val location: Location
) : LambdaCallNode(lambda.cast(), arguments.cast<List<KSharpNode>>().toTypedArray(), type), IrExpression {
    override val serializer: IrNodeSerializers
        get() = IrNodeSerializers.LambdaCall

}
