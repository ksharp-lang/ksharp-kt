package org.ksharp.ir.transform

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.ir.*
import org.ksharp.nodes.semantic.*
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.types.*
import java.util.concurrent.atomic.AtomicInteger

typealias CustomApplicationIrNode = ApplicationNode<SemanticInfo>.(state: IrState) -> IrExpression

val IrIfFactory: CustomApplicationIrNode = { state ->
    val (attributes, symbols) = arguments.toIrSymbols(state)
    IrIf(
        attributes,
        symbols[0],
        symbols[1],
        symbols[2],
        location
    )
}

val IrNumCastFactory: CustomApplicationIrNode = { state ->
    val (_, symbols) = arguments.toIrSymbols(state)

    IrNumCast(
        symbols[0],
        when (this.functionName.name) {
            "byte" -> CastType.Byte
            "short" -> CastType.Short
            "int" -> CastType.Int
            "long" -> CastType.Long
            "float" -> CastType.Float
            "double" -> CastType.Double
            "bigint" -> CastType.BigInt
            else -> CastType.BigDecimal
        },
        location
    )
}

val IrToStringFactory: CustomApplicationIrNode = { state ->
    val (_, symbols) = arguments.toIrSymbols(state)
    IrToString(
        symbols[0],
        location
    )
}

private var irNodeFactory = mapOf(
    "prelude::listOf" to IrListFactory,
    "prelude::setOf" to IrSetFactory,
    "prelude::arrayOf" to IrArrayFactory,
    "prelude::mapOf" to IrMapFactory,
    "prelude::bool" to IrBoolFactory,
    "prelude::pair" to binaryOperationFactory(::IrPair),
    "prelude::num::(+)/2" to binaryOperationFactory(::IrSum),
    "prelude::num::(-)/2" to binaryOperationFactory(::IrSub),
    "prelude::num::(*)/2" to binaryOperationFactory(::IrMul),
    "prelude::num::(/)/2" to binaryOperationFactory(::IrDiv),
    "prelude::num::(**)/2" to binaryOperationFactory(::IrPow),
    "prelude::num::(%)/2" to binaryOperationFactory(::IrMod),
    "prelude::comparable::lt" to relationalOperationFactory(::IrLt, Less),
    "prelude::comparable::le" to relationalOperationFactory(::IrLe, Less, Equal),
    "prelude::comparable::ge" to relationalOperationFactory(::IrGe, Greater, Equal),
    "prelude::comparable::gt" to relationalOperationFactory(::IrGt, Greater),
    "prelude::equals" to equalsOperationFactory(::IrEq, ::IrEquals),
    "prelude::not-equals" to equalsOperationFactory(::IrNotEq, ::IrNotEquals),
    "prelude::bit::(&)/2" to binaryOperationFactory(::IrBitAnd),
    "prelude::bit::(|)/2" to binaryOperationFactory(::IrBitOr),
    "prelude::bit::(^)/2" to binaryOperationFactory(::IrBitXor),
    "prelude::bit::(>>)/2" to binaryOperationFactory(::IrBitShr),
    "prelude::bit::(<<)/2" to binaryOperationFactory(::IrBitShl),
    "prelude::num-cast" to IrNumCastFactory,
    "prelude::if" to IrIfFactory,
    "prelude::to-string" to IrToStringFactory
)

fun computeAttributes(expressionsCounter: Int, constantCounter: Int, pureCounter: Int): Set<Attribute> {
    val attributes = mutableSetOf<Attribute>()
    if (constantCounter == expressionsCounter) {
        attributes.add(CommonAttribute.Constant)
    }
    attributes.add(if (pureCounter == expressionsCounter) CommonAttribute.Pure else CommonAttribute.Impure)
    return attributes
}

fun SemanticNode<SemanticInfo>.isUnitConstant() =
    this is ConstantNode && this.value == Unit

fun List<SemanticNode<SemanticInfo>>.toIrSymbols(
    state: IrState
): Pair<Set<Attribute>, List<IrExpression>> {
    val constantCounter = AtomicInteger()
    val pureCounter = AtomicInteger()
    val symbols = if (this.size == 1 && this.first().isUnitConstant()) {
        emptyList()
    } else this.map {
        val symbol = it.toIrSymbol(state)
        val symbolAttrs = symbol.attributes
        val isConstant = symbolAttrs.contains(CommonAttribute.Constant)
        if (isConstant) {
            constantCounter.incrementAndGet()
        }
        if (isConstant || symbolAttrs.contains(CommonAttribute.Pure)) {
            pureCounter.incrementAndGet()
        }
        symbol
    }
    return computeAttributes(symbols.size, constantCounter.get(), pureCounter.get()) to symbols
}


fun Type.asTraitType(): TraitType? =
    when (this) {
        is TraitType -> this
        is ImplType -> this.trait
        is FixedTraitType -> this.trait
        is ParametricType -> this.type.asTraitType()
        else -> null
    }


private fun ApplicationSemanticInfo.isUnionOrConstructor(inferredType: Type): Boolean =
    (function == null)
            && ((inferredType is UnionType) || (inferredType is TypeConstructor))

private fun ApplicationSemanticInfo.isATraitFunction(): Boolean =
    function != null && function!!.attributes.contains(CommonAttribute.TraitMethod)

private fun ApplicationSemanticInfo.traitType(): TraitType? =
    function!!.arguments.first().asTraitType()

val ApplicationNode<SemanticInfo>.customIrNode: String?
    get() =
        info.cast<ApplicationSemanticInfo>().let { info ->
            info.function.irCustomNode ?: with(inferredType) {
                when {
                    info.isUnionOrConstructor(inferredType) -> irCustomNode
                    info.isATraitFunction() -> {
                        info.traitType()?.let { traitType ->
                            val traitCustomNode = traitType.irCustomNode
                            if (traitCustomNode != null) "$traitCustomNode::${functionName.name}/${info.function!!.arguments.arity}"
                            else null
                        }
                    }

                    else -> null
                }
            }
        }

private fun toIrCallSymbol(
    functionType: FunctionType,
    attributes: Set<Attribute>,
    callName: String,
    arguments: List<IrExpression>,
    returnType: Type,
    location: Location,
): IrCall {
    val trait = functionType.arguments.first().asTraitType()
    val isTrait = trait != null
    val traitName = if (isTrait) {
        trait?.name
    } else null
    val scopeName = if (isTrait) {
        trait?.irCustomNode
    } else null
    return IrCall(
        attributes,
        null,
        CallScope(callName, traitName, scopeName),
        arguments,
        returnType,
        location,
    )
}

fun cleanSymbols(value: String): String {
    return value.replace(Regex("([-?])")) {
        when (val v = it.groupValues.first()) {
            "-" -> "DASH"
            "?" -> "QUESTION"
            else -> v
        }
    }
}

fun nativeModuleName(moduleName: String) = moduleName.replace("([A-Z])".toRegex(), "_$1").lowercase()

fun nativeApplicationName(moduleName: String, callName: String) =
    "${nativeModuleName(moduleName)}.${
        cleanSymbols(callName.replace("/", "")
            .replaceFirstChar { it.uppercaseChar() })
    }"

fun ApplicationNode<SemanticInfo>.toIrSymbol(
    state: IrState,
    symbol: Symbol
): IrExpression {
    val lambda = state.variableIndex[symbol.name]!!.toIrSymbol(location)
    val (attributes, arguments) = arguments.toIrSymbols(state)
    return IrLambdaCall(
        attributes,
        lambda,
        arguments,
        info.getInferredType(location).valueOrNull!!,
        location
    )
}

fun ApplicationNode<SemanticInfo>.toIrSymbol(
    state: IrState
): IrExpression {
    val info = this.info.cast<ApplicationSemanticInfo>()
    if (info.functionSymbol != null) {
        return toIrSymbol(state, info.functionSymbol!!)
    }
    val factory = customIrNode?.let {
        irNodeFactory[it] ?: throw RuntimeException("Ir symbol factory $it not found")
    }
    return if (factory != null) factory(state)
    else {
        val (attributes, arguments) = arguments.toIrSymbols(state)
        val functionType = info.function!!
        val callName = "${functionName.name}/${functionType.arguments.arity}"
        val functionModuleName =
            if (functionName.pck == null) state.moduleName
            else state.module.dependencies[functionName.pck]!!
        val returnType = info.getInferredType(location).valueOrNull!!
        when {
            functionName.pck != null ->
                IrModuleCall(
                    attributes,
                    state.loader,
                    functionModuleName,
                    callName,
                    arguments,
                    functionType,
                    location
                )

            functionType.attributes.contains(CommonAttribute.Native) ->
                IrNativeCall(
                    attributes,
                    nativeApplicationName(functionModuleName, callName),
                    arguments,
                    returnType,
                    location,
                )

            else ->
                toIrCallSymbol(
                    functionType,
                    attributes,
                    callName,
                    arguments,
                    returnType,
                    location
                ).apply { this.functionLookup = state.functionLookup }
        }
    }
}
