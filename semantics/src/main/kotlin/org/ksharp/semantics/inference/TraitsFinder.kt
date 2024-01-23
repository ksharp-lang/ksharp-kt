package org.ksharp.semantics.inference

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.common.isRight
import org.ksharp.module.FunctionInfo
import org.ksharp.module.Impl
import org.ksharp.module.ModuleInfo
import org.ksharp.module.prelude.preludeModule
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.substitution.SubstitutionContext
import org.ksharp.typesystem.substitution.extract
import org.ksharp.typesystem.substitution.substitute
import org.ksharp.typesystem.types.*
import org.ksharp.typesystem.unification.UnificationChecker
import org.ksharp.typesystem.unification.unify

private class FunctionTypeInfo(
    override val name: String,
    function: FunctionType,
    override val arity: Int
) : FunctionInfo {
    override val attributes: Set<Attribute> = function.attributes
    override val types: List<Type> = function.arguments
}

internal fun methodTypeToFunctionInfo(
    trait: TraitType,
    method: TraitType.MethodType,
    checker: UnificationChecker
): InferenceFunctionInfo {
    val fnType = method.arguments.toFunctionType(trait.typeSystem.handle!!, method.attributes, method.scope)
    val substitutionContext = SubstitutionContext(checker)
    substitutionContext.extract(Location.NoProvided, fnType, fnType)
    substitutionContext.addMapping(Location.NoProvided, trait.param, trait.toParametricType())
    val type =
        substitutionContext.substitute(Location.NoProvided, fnType, fnType).valueOrNull!!.cast<FunctionType>()
    return InferenceFunctionInfo(FunctionTypeInfo(method.name, type, type.arguments.arity), method.scope)
}

class TraitFinderContext(
    val typeSystem: TypeSystem,
    val impls: Sequence<Impl>
) {

    fun findTraitFunction(methodName: String, type: Type): InferenceFunctionInfo? =
        getTraitsImplemented(type, this).mapNotNull { trait ->
            trait.methods[methodName]?.let {
                methodTypeToFunctionInfo(trait, it, unificationChecker(this))
            }
        }.firstOrNull()

    fun findPartialTraitFunction(methodName: String, numParams: Int, type: Type): Sequence<InferenceFunctionInfo> =
        unificationChecker(this).let { checker ->
            "$methodName/".let { prefixName ->
                getTraitsImplemented(type, this).map { trait ->
                    trait.methods
                        .asSequence()
                        .filter { (key, value) ->
                            key.startsWith(prefixName) && value.arguments.arity > numParams
                        }.map {
                            methodTypeToFunctionInfo(trait, it.value, checker)
                        }
                }.flatten()
            }
        }

}


fun unificationChecker(context: TraitFinderContext) = UnificationChecker { trait, type ->
    val checkType = when (type) {
        is FixedTraitType -> type.trait
        is ImplType -> type.impl
        else -> type
    }
    if (trait == checkType) {
        true
    } else sequenceOf(
        getTraitsImplemented(checkType, context),
        getTraitsImplemented(checkType, preludeTraitFinderContext)
    )
        .flatten()
        .any { t ->
            trait == t
        }
}

private fun Sequence<Impl>.filterTraits(
    location: Location,
    type: Type,
    typeSystem: TypeSystem,
    checker: UnificationChecker
): Sequence<TraitType> =
    filter { it.type.unify(location, type, checker).isRight }
        .map {
            typeSystem[it.trait].valueOrNull!!
        }.cast()

private fun findTraits(type: Type, context: TraitFinderContext): Sequence<TraitType> =
    unificationChecker(context).let { checker ->
        context.impls
            .filterTraits(Location.NoProvided, type, context.typeSystem, checker)
    }

private val ParametricType.traitOrNull: TraitType?
    get() =
        if (type is TraitType) type.cast()
        else typeSystem
            .handle!![type.representation]
            .valueOrNull as? TraitType

fun getTraitsImplemented(type: Type, context: TraitFinderContext): Sequence<TraitType> =
    type().map { resolvedType ->
        when {
            resolvedType is Parameter ->
                context.typeSystem.asSequence()
                    .map { it.second }
                    .filterIsInstance<TraitType>()

            resolvedType is ParametricType && resolvedType.params.size == 1 -> {
                resolvedType.traitOrNull?.let {
                    sequenceOf(it)
                } ?: findTraits(resolvedType, context)
            }

            resolvedType is TraitType -> sequenceOf(resolvedType)
            else -> findTraits(resolvedType, context)
        }
    }.valueOrNull ?: emptySequence()

val ModuleInfo.traitFinderContext
    get() =
        TraitFinderContext(typeSystem, impls.asSequence())

val preludeTraitFinderContext = preludeModule.traitFinderContext
