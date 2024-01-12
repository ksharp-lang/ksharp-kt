package org.ksharp.ir.transform

import org.ksharp.common.cast
import org.ksharp.ir.*
import org.ksharp.nodes.semantic.*
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.attributes.NameAttribute
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

private var irNodeFactory = mapOf<String, CustomApplicationIrNode>(
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
    "prelude::num::lt" to binaryOperationFactory(::IrLt),
    "prelude::num::le" to binaryOperationFactory(::IrLe),
    "prelude::num::ge" to binaryOperationFactory(::IrGe),
    "prelude::num::gt" to binaryOperationFactory(::IrGt),
    "prelude::equals" to equalsOperationFactory(::IrEq, ::IrEquals),
    "prelude::not-equals" to equalsOperationFactory(::IrNotEq, ::IrNotEquals),
    "prelude::bit::(&)/2" to binaryOperationFactory(::IrBitAnd),
    "prelude::bit::(|)/2" to binaryOperationFactory(::IrBitOr),
    "prelude::bit::(^)/2" to binaryOperationFactory(::IrBitXor),
    "prelude::bit::(>>)/2" to binaryOperationFactory(::IrBitShr),
    "prelude::bit::(<<)/2" to binaryOperationFactory(::IrBitShl),
    "prelude::num-cast" to IrNumCastFactory,
    "prelude::if" to IrIfFactory
)

fun computeAttributes(exprsCounter: Int, constantCounter: Int, pureCounter: Int): Set<Attribute> {
    val attributes = mutableSetOf<Attribute>()
    if (constantCounter == exprsCounter) {
        attributes.add(CommonAttribute.Constant)
    }
    attributes.add(if (pureCounter == exprsCounter) CommonAttribute.Pure else CommonAttribute.Impure)
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

val Type?.irCustomNode: String?
    get() =
        if (this != null) attributes.firstOrNull { a -> a is NameAttribute }
            ?.let { a -> a.cast<NameAttribute>().value["ir"] }
        else null

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

fun ApplicationNode<SemanticInfo>.toIrSymbol(
    state: IrState
): IrExpression {
    val factory = customIrNode?.let {
        irNodeFactory[it] ?: throw RuntimeException("Ir symbol factory $it not found")
    }
    return if (factory != null) factory(state)
    else {
        val info = this.info.cast<ApplicationSemanticInfo>()
        val (attributes, arguments) = arguments.toIrSymbols(state)
        val functionType = info.function!!
        val callName = "${functionName.name}/${functionType.arguments.arity}"
        val trait = functionType.arguments.first().asTraitType()
        val isTrait = trait != null
        val scopeName = if (isTrait) {
            trait?.irCustomNode
        } else null
        IrCall(
            attributes,
            null,
            CallScope(callName, scopeName, isTrait),
            arguments,
            location,
        ).apply {
            this.functionLookup = state.functionLookup
        }
    }
}
