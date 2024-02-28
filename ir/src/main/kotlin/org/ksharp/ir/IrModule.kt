package org.ksharp.ir

import org.ksharp.common.Location
import org.ksharp.common.cacheOf
import org.ksharp.common.cast
import org.ksharp.ir.serializer.IrNodeSerializers
import org.ksharp.ir.transform.BinaryOperationFactory
import org.ksharp.ir.transform.abstractionToIrSymbol
import org.ksharp.module.CodeModule
import org.ksharp.module.Impl
import org.ksharp.module.ModuleInfo
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.nodes.semantic.SemanticInfo
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.attributes.NameAttribute
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.types.Type

class IrModuleInfo(
    val moduleInfo: ModuleInfo,
    val irModule: IrModule
)

fun interface LoadIrModuleFn {
    fun load(name: String): IrModuleInfo?
}

fun interface FunctionLookup {
    fun find(module: String?, call: CallScope, firstValue: Type?): IrTopLevelSymbol
}

internal val Type?.irCustomNode: String?
    get() =
        if (this != null) attributes.firstOrNull { a -> a is NameAttribute }
            ?.let { a -> a.cast<NameAttribute>().value["ir"] }
        else null

private fun binaryExpressionFunction(
    name: String,
    factory: BinaryOperationFactory
): () -> IrTopLevelSymbol = {
    IrArithmeticCall(
        name,
        factory(
            NoAttributes,
            IrArg(NoAttributes, NoCaptured, 0, Location.NoProvided),
            IrArg(NoAttributes, NoCaptured, 1, Location.NoProvided),
            Location.NoProvided
        ).cast(),
    )
}

private class FunctionLookupImpl : FunctionLookup {

    lateinit var functions: List<IrTopLevelSymbol>
    lateinit var traits: Map<String, List<IrTopLevelSymbol>>
    lateinit var impls: Map<Impl, List<IrTopLevelSymbol>>

    private val cache = cacheOf<Pair<Type?, CallScope>, IrTopLevelSymbol>()

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
        if (call.traitScopeName != null) {
            return irNodeFactory["${call.traitScopeName}::${call.callName}"]?.invoke()
        }
        return null
    }

    private fun List<IrTopLevelSymbol>?.findFunction(call: CallScope): IrTopLevelSymbol? =
        if (this == null) null
        else asSequence()
            .filter { it.name == call.callName }
            .firstOrNull()

    private fun Impl.findImplFunction(call: CallScope): IrTopLevelSymbol? =
        impls[this].findFunction(call)

    private fun String?.findTraitFunction(call: CallScope): IrTopLevelSymbol? =
        if (this == null) null
        else traits[this].findFunction(call)

    private fun findFunction(call: CallScope, firstValue: Type?): IrTopLevelSymbol? =
        (if (call.traitName != null && firstValue != null) {
            Impl("", call.traitName, firstValue).findImplFunction(call) ?: call.traitName.findTraitFunction(call)
        } else null)
            ?: functions.findFunction(call)

    override fun find(module: String?, call: CallScope, firstValue: Type?): IrTopLevelSymbol =
        cache.get(firstValue to call) {
            findFunction(call, firstValue) ?: findCustomFunction(call)!!
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
    state: PartialIrState
) =
    asSequence()
        .filterNot { it.attributes.contains(CommonAttribute.Native) }
        .map { it.abstractionToIrSymbol(state) }
        .toList()

fun CodeModule.toIrModule(loader: LoadIrModuleFn): IrModule {
    val lookup = functionLookup()
    val state = PartialIrState(name, module, loader, lookup)
    val module = IrModule(
        artifact.abstractions
            .mapToIrSymbols(state),
        traitArtifacts
            .mapValues { entry ->
                entry.value.abstractions
                    .mapToIrSymbols(state)
            },
        implArtifacts
            .mapValues { entry ->
                entry.value.abstractions
                    .mapToIrSymbols(state)
            }
    )
    lookup.link(module)
    return module
}
