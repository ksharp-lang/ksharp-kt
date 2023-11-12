package org.ksharp.ir

import org.ksharp.common.Either
import org.ksharp.common.Location
import org.ksharp.common.cacheOf
import org.ksharp.common.cast
import org.ksharp.ir.transform.toIrSymbol
import org.ksharp.semantics.nodes.SemanticModuleInfo
import org.ksharp.typesystem.types.FunctionType
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.unification.unify

fun interface FunctionLookup {
    fun find(module: String?, name: String, type: Type): IrTopLevelSymbol?
}

private class FunctionLookupImpl : FunctionLookup {

    lateinit var functions: List<IrTopLevelSymbol>

    private val cache = cacheOf<Pair<String, Type>, IrTopLevelSymbol>()

    override fun find(module: String?, name: String, type: Type): IrTopLevelSymbol =
        cache.get(name to type) {
            val functionType = type.cast<FunctionType>()
            val arity = functionType.arguments.size
            functions.asSequence()
                .filter { it.name == name && it.type.arguments.size == arity }
                .map { fn ->
                    functionType.unify(Location.NoProvided, type) { _, _ -> false }.map { fn }
                }
                .firstOrNull { it is Either.Right }
                ?.valueOrNull!!
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
