package org.ksharp.ir.transform

import org.ksharp.common.cast
import org.ksharp.ir.IrExpression
import org.ksharp.ir.IrPair
import org.ksharp.nodes.semantic.ApplicationNode
import org.ksharp.nodes.semantic.SemanticNode
import org.ksharp.semantics.nodes.ApplicationSemanticInfo
import org.ksharp.semantics.nodes.SemanticInfo
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.attributes.NameAttribute
import java.util.concurrent.atomic.AtomicInteger

typealias CustomApplicationIrNode = ApplicationNode<SemanticInfo>.() -> IrExpression

private var irNodeFactory = mapOf<String, CustomApplicationIrNode>(
    "prelude::listOf" to IrListFactory,
    "prelude::setOf" to IrSetFactory,
    "prelude::mapOf" to IrMapFactory,
    "prelude::pair" to {
        val (attributes, symbols) = arguments.toIrSymbols()
        IrPair(
            attributes,
            symbols.first(),
            symbols.last(),
            location
        )
    }
)

fun List<SemanticNode<SemanticInfo>>.toIrSymbols(): Pair<Set<Attribute>, List<IrExpression>> {
    val constantCounter = AtomicInteger()
    val pureCounter = AtomicInteger()
    val symbols = this.map {
        val symbol = it.toIrSymbol()
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

val ApplicationNode<SemanticInfo>.customIrNode: String?
    get() =
        info.cast<ApplicationSemanticInfo>().function?.let {
            it.attributes.firstOrNull { a -> a is NameAttribute }
                ?.let { a -> a.cast<NameAttribute>().value["ir"] }
        }

fun ApplicationNode<SemanticInfo>.toIrSymbol(): IrExpression {
    val factory = customIrNode?.let {
        irNodeFactory[it] ?: throw RuntimeException("Ir symbol factory $it not found")
    }
    return if (factory != null) factory()
    else TODO()
}
