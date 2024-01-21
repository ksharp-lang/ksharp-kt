package org.ksharp.semantics.context

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.nodes.FunctionNode
import org.ksharp.semantics.expressions.nameWithArity
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.substitution.SubstitutionContext
import org.ksharp.typesystem.substitution.extract
import org.ksharp.typesystem.substitution.substitute
import org.ksharp.typesystem.types.FunctionType
import org.ksharp.typesystem.types.TraitType
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.types.toFunctionType
import org.ksharp.typesystem.unification.UnificationChecker

interface SemanticContext {

    val typeSystem: TypeSystem

    fun findFunctionType(name: String): FunctionType?

    fun calculateVisibility(function: FunctionNode): CommonAttribute

}

class TypeSystemSemanticContext(override val typeSystem: TypeSystem) : SemanticContext {
    override fun findFunctionType(name: String): FunctionType? =
        typeSystem["Decl__$name"].valueOrNull as? FunctionType

    override fun calculateVisibility(function: FunctionNode): CommonAttribute =
        if (function.pub) CommonAttribute.Public else CommonAttribute.Internal
}

class TraitSemanticContext(override val typeSystem: TypeSystem, private val trait: TraitType) : SemanticContext {
    override fun findFunctionType(name: String): FunctionType? =
        trait.methods[name]?.let {
            it.arguments.toFunctionType(it.typeSystem.handle!!, it.attributes, it.scope)
        }

    override fun calculateVisibility(function: FunctionNode): CommonAttribute {
        val name = function.nameWithArity
        return if (trait.methods.contains(name)) {
            CommonAttribute.Public
        } else CommonAttribute.Internal
    }
}


class ImplSemanticContext(
    override val typeSystem: TypeSystem,
    private val location: Location,
    private val forType: Type,
    private val trait: TraitType,
    private val checker: UnificationChecker
) : SemanticContext {
    override fun findFunctionType(name: String): FunctionType? =
        trait.methods[name]?.let {
            val fnType = it.arguments.toFunctionType(it.typeSystem.handle!!, it.attributes, it.scope)
            val substitutionContext = SubstitutionContext(checker)
            substitutionContext.extract(location, fnType, fnType)
            substitutionContext.addMapping(location, trait.param, forType)
            substitutionContext.substitute(location, fnType, fnType).valueOrNull!!.cast()
        }

    override fun calculateVisibility(function: FunctionNode): CommonAttribute {
        val name = function.nameWithArity
        return if (trait.methods.contains(name)) {
            CommonAttribute.Public
        } else CommonAttribute.Internal
    }
}
