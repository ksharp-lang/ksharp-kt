package org.ksharp.ir

import org.ksharp.common.Location
import org.ksharp.common.cacheOf
import org.ksharp.common.cast
import org.ksharp.common.isRight
import org.ksharp.ir.transform.BinaryOperationFactory
import org.ksharp.ir.transform.asTraitType
import org.ksharp.ir.transform.irCustomNode
import org.ksharp.ir.transform.toIrSymbol
import org.ksharp.semantics.nodes.SemanticModuleInfo
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.types.FunctionType
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.types.arity
import org.ksharp.typesystem.unification.unify

fun interface FunctionLookup {
    fun find(module: String?, name: String, type: Type): IrTopLevelSymbol?
}

private fun binaryExpressionFunction(
    name: String,
    factory: BinaryOperationFactory
): (type: FunctionType) -> IrTopLevelSymbol = {
    IrArithmeticCall(
        name,
        factory(
            NoAttributes,
            IrArg(NoAttributes, 0, Location.NoProvided),
            IrArg(NoAttributes, 1, Location.NoProvided),
            Location.NoProvided
        ).cast(),
        it
    )
}

private class FunctionLookupImpl : FunctionLookup {

    lateinit var functions: List<IrTopLevelSymbol>

    private val cache = cacheOf<Pair<String, Type>, IrTopLevelSymbol>()

    private var irNodeFactory = mapOf(
        "prelude::sum::(+)" to binaryExpressionFunction("(+)", ::IrSum),
        "prelude::sub::(-)" to binaryExpressionFunction("(-)", ::IrSub),
        "prelude::mul::(*)" to binaryExpressionFunction("(-)", ::IrMul),
        "prelude::div::(/)" to binaryExpressionFunction("(-)", ::IrDiv),
        "prelude::pow::(**)" to binaryExpressionFunction("(-)", ::IrPow),
        "prelude::mod::(%)" to binaryExpressionFunction("(-)", ::IrMod),
    )

    private fun findCustomFunction(name: String, type: FunctionType): IrTopLevelSymbol? {
        if (type.attributes.contains(CommonAttribute.TraitMethod)) {
            val trait = type.arguments.first().asTraitType()
            if (trait != null) {
                val node = trait.irCustomNode
                return irNodeFactory["$node::$name"]?.invoke(type)
            }
        }
        return null
    }

    override fun find(module: String?, name: String, type: Type): IrTopLevelSymbol =
        cache.get(name to type) {
            val functionType = type.cast<FunctionType>()
            val arity = functionType.arguments.arity
            functions.asSequence()
                .filter {
                    it.name == name && it.type.arguments.arity == arity
                }
                .map { fn ->
                    functionType.unify(Location.NoProvided, type) { _, _ -> false }.map { fn }
                }
                .firstOrNull { it.isRight }
                ?.valueOrNull
                ?: findCustomFunction(name, functionType)!!
        }

}

data class IrModule(
    val dependencies: List<String>,
    val symbols: List<IrTopLevelSymbol>
) : IrNode

fun SemanticModuleInfo.toIrModule(): Pair<IrModule, FunctionLookup> {
    val lookup = FunctionLookupImpl()
    val module = IrModule(
        listOf(),
        abstractions.map { it.toIrSymbol(lookup) }
    )
    lookup.functions = module.symbols.cast()
    return module to lookup
}
