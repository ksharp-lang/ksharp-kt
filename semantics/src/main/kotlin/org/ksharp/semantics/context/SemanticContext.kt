package org.ksharp.semantics.context

import org.ksharp.nodes.FunctionNode
import org.ksharp.semantics.expressions.nameWithArity
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.types.FunctionType
import org.ksharp.typesystem.types.TraitType
import org.ksharp.typesystem.types.toFunctionType

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
            it.arguments.toFunctionType(it.typeSystem.handle!!, it.attributes)
        }

    override fun calculateVisibility(function: FunctionNode): CommonAttribute {
        val name = function.nameWithArity
        return if (trait.methods.contains(name)) {
            CommonAttribute.Public
        } else CommonAttribute.Internal
    }
}
