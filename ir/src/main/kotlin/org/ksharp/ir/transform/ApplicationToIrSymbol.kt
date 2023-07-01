package org.ksharp.ir.transform

import org.ksharp.common.cast
import org.ksharp.ir.*
import org.ksharp.nodes.semantic.ApplicationNode
import org.ksharp.nodes.semantic.SemanticNode
import org.ksharp.semantics.nodes.ApplicationSemanticInfo
import org.ksharp.semantics.nodes.SemanticInfo
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.attributes.NameAttribute
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.types.UnionType
import java.util.concurrent.atomic.AtomicInteger

typealias CustomApplicationIrNode = ApplicationNode<SemanticInfo>.(functionLookup: FunctionLookup, variableIndex: VariableIndex) -> IrExpression

val IrIfFactory: CustomApplicationIrNode = { fLookup, variableIndex ->
    val (attributes, symbols) = arguments.toIrSymbols(fLookup, variableIndex)
    IrIf(
        attributes,
        symbols[0],
        symbols[1],
        symbols[2],
        location
    )
}

val IrNumCastFactory: CustomApplicationIrNode = { fLookup, variableIndex ->
    val (_, symbols) = arguments.toIrSymbols(fLookup, variableIndex)

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
    "prelude::mapOf" to IrMapFactory,
    "prelude::bool" to IrBoolFactory,
    "prelude::pair" to binaryOperationFactory(::IrPair),
    "prelude::sum" to binaryOperationFactory(::IrSum),
    "prelude::sub" to binaryOperationFactory(::IrSub),
    "prelude::mul" to binaryOperationFactory(::IrMul),
    "prelude::div" to binaryOperationFactory(::IrDiv),
    "prelude::pow" to binaryOperationFactory(::IrPow),
    "prelude::mod" to binaryOperationFactory(::IrMod),
    "prelude::num-cast" to IrNumCastFactory,
    "prelude::if" to IrIfFactory
)


fun List<SemanticNode<SemanticInfo>>.toIrSymbols(
    functionLookup: FunctionLookup,
    variableIndex: VariableIndex
): Pair<Set<Attribute>, List<IrExpression>> {
    val constantCounter = AtomicInteger()
    val pureCounter = AtomicInteger()
    val symbols = this.map {
        val symbol = it.toIrSymbol(functionLookup, variableIndex)
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
    val attributes = mutableSetOf<Attribute>()
    val expressions = symbols.size
    if (constantCounter.get() == expressions) {
        attributes.add(CommonAttribute.Constant)
    }
    attributes.add(if (pureCounter.get() == expressions) CommonAttribute.Pure else CommonAttribute.Impure)
    return attributes to symbols
}

private val Type?.irCustomNode: String?
    get() =
        if (this != null) attributes.firstOrNull { a -> a is NameAttribute }
            ?.let { a -> a.cast<NameAttribute>().value["ir"] }
        else null

val ApplicationNode<SemanticInfo>.customIrNode: String?
    get() =
        info.cast<ApplicationSemanticInfo>().let { info ->
            info.function.irCustomNode ?: with(inferredType) {
                if (info.function == null && this is UnionType) irCustomNode else null
            }
        }

fun ApplicationNode<SemanticInfo>.toIrSymbol(
    functionLookup: FunctionLookup,
    variableIndex: VariableIndex
): IrExpression {
    val factory = customIrNode?.let {
        irNodeFactory[it] ?: throw RuntimeException("Ir symbol factory $it not found")
    }
    return if (factory != null) factory(functionLookup, variableIndex)
    else {
        val info = this.info.cast<ApplicationSemanticInfo>()
        val (attributes, arguments) = arguments.toIrSymbols(functionLookup, variableIndex)
        IrCall(
            attributes,
            null,
            functionName.name,
            arguments,
            info.function!!,
            location,
        ).apply {
            this.functionLookup = functionLookup
        }
    }
}
