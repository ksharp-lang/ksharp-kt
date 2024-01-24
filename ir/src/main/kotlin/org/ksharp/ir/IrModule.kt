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
import org.ksharp.typesystem.attributes.NameAttribute
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.types.FixedTraitType
import org.ksharp.typesystem.types.ImplType
import org.ksharp.typesystem.types.TraitType
import org.ksharp.typesystem.types.Type

fun interface FunctionLookup {
    fun find(module: String?, call: CallScope, firstValue: Type?): IrTopLevelSymbol?
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
        if (call.traitType != null) {
            return irNodeFactory["${call.traitType.asTraitType!!.irCustomNode}::${call.callName}"]?.invoke()
        }
        return null
    }

    private val ImplType.implInstance get() = Impl(this.trait.name, this.impl)

    private val Type.asTraitType
        get() =
            when (this) {
                is ImplType -> this.trait
                is TraitType -> this
                is FixedTraitType -> this.trait
                else -> null
            }

    private val Type.traitName
        get() =
            when (this) {
                is ImplType -> this.trait.name
                is TraitType -> this.name
                is FixedTraitType -> this.trait.name
                else -> null
            }

    private fun List<IrTopLevelSymbol>?.findFunction(call: CallScope): IrTopLevelSymbol? =
        if (this == null) null
        else asSequence()
            .filter { it.name == call.callName }
            .firstOrNull()

    private fun ImplType.findImplFunction(call: CallScope): IrTopLevelSymbol? =
        impls[this.implInstance].findFunction(call)

    private fun CallScope.withType(firstValue: Type?): CallScope =
        if (this.traitType != null) {
            if (this.traitType is ImplType) {
                this
            } else {
                copy(traitType = ImplType(this.traitType.asTraitType!!, firstValue!!))
            }
        } else this

    private fun String?.findTraitFunction(call: CallScope): IrTopLevelSymbol? =
        if (this == null) null
        else traits[this].findFunction(call)

    private fun findFunction(call: CallScope): IrTopLevelSymbol? =
        (if (call.traitType != null) {
            val type = call.traitType
            when {
                type is ImplType -> type.findImplFunction(call) ?: type.traitName.findTraitFunction(call)
                else -> type.traitName.findTraitFunction(call)
            }
        } else null)
            ?: functions.findFunction(call)

    override fun find(module: String?, call: CallScope, firstValue: Type?): IrTopLevelSymbol =
        cache.get(call.withType(firstValue)) {
            findFunction(call) ?: findCustomFunction(call)!!
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
