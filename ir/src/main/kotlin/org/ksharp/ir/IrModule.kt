package org.ksharp.ir

import org.ksharp.common.Location
import org.ksharp.common.cacheOf
import org.ksharp.common.cast
import org.ksharp.ir.serializer.IrNodeSerializers
import org.ksharp.ir.transform.BinaryOperationFactory
import org.ksharp.ir.transform.toIrSymbol
import org.ksharp.module.CodeModule
import org.ksharp.module.Impl
import org.ksharp.module.ModuleInfo
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.nodes.semantic.SemanticInfo
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.attributes.NoAttributes

fun interface FunctionLookup {
    fun find(module: String?, call: CallScope): IrTopLevelSymbol?
}

private fun binaryExpressionFunction(
    name: String,
    factory: BinaryOperationFactory
): () -> IrTopLevelSymbol = {
    IrArithmeticCall(
        name,
        factory(
            NoAttributes,
            IrArg(NoAttributes, 0, Location.NoProvided),
            IrArg(NoAttributes, 1, Location.NoProvided),
            Location.NoProvided
        ).cast(),
    )
}

private class FunctionLookupImpl : FunctionLookup {

    lateinit var functions: List<IrTopLevelSymbol>
    lateinit var traits: Map<String, List<IrTopLevelSymbol>>
    lateinit var impls: Map<Impl, List<IrTopLevelSymbol>>

    private val cache = cacheOf<CallScope, IrTopLevelSymbol>()

    private var irNodeFactory = mapOf(
        "prelude::num::(+)/2" to binaryExpressionFunction("(+)", ::IrSum),
        "prelude::num::(-)/2" to binaryExpressionFunction("(-)", ::IrSub),
        "prelude::num::(*)/2" to binaryExpressionFunction("(*)", ::IrMul),
        "prelude::num::(/)/2" to binaryExpressionFunction("(/)", ::IrDiv),
        "prelude::num::(**)/2" to binaryExpressionFunction("(**)", ::IrPow),
        "prelude::num::(%)/2" to binaryExpressionFunction("(%)", ::IrMod),

        "prelude::bit::(&)/2" to binaryExpressionFunction("(&)", ::IrBitAnd),
        "prelude::bit::(|)/2" to binaryExpressionFunction("(|)", ::IrBitOr),
        "prelude::bit::(^)/2" to binaryExpressionFunction("(^)", ::IrBitXor),
        "prelude::bit::(>>)/2" to binaryExpressionFunction("(>>)", ::IrBitShr),
        "prelude::bit::(<<)/2" to binaryExpressionFunction("(<<)", ::IrBitShl),
    )

    private fun findCustomFunction(call: CallScope): IrTopLevelSymbol? {
        if (call.isFirstArgTrait) {
            return irNodeFactory["${call.traitScopeName}::${call.callName}"]?.invoke()
        }
        return null
    }

    override fun find(module: String?, call: CallScope): IrTopLevelSymbol =
        cache.get(call) {
            functions.asSequence()
                .filter {
                    it.name == call.callName
                }
                .firstOrNull()
                ?: findCustomFunction(call)!!
        }

}

fun functionLookup(): FunctionLookup = FunctionLookupImpl()

fun FunctionLookup.link(module: IrModule) {
    if (this is FunctionLookupImpl) {
        functions = module.symbols.cast()
        traits = module.traitSymbols.cast()
        impls = module.implSymbols.cast()
    }
}

data class IrModule(
    val symbols: List<IrTopLevelSymbol>,
    val traitSymbols: Map<String, List<IrTopLevelSymbol>>,
    val implSymbols: Map<Impl, List<IrTopLevelSymbol>>,
) : IrNode {
    override val serializer: IrNodeSerializers = IrNodeSerializers.Module
}

private fun List<AbstractionNode<SemanticInfo>>.mapToIrSymbols(
    name: String,
    module: ModuleInfo,
    lookup: FunctionLookup
) =
    asSequence()
        .filterNot { it.attributes.contains(CommonAttribute.Native) }
        .map { it.toIrSymbol(name, module.dependencies, lookup) }
        .toList()

fun CodeModule.toIrModule(): IrModule {
    val lookup = functionLookup()
    val module = IrModule(
        artifact.abstractions
            .mapToIrSymbols(name, module, lookup),
        traitArtifacts
            .mapValues { entry ->
                entry.value.abstractions
                    .mapToIrSymbols(name, module, lookup)
            },
        implArtifacts
            .mapValues { entry ->
                entry.value.abstractions
                    .mapToIrSymbols(name, module, lookup)
            }
    )
    lookup.link(module)
    return module
}
