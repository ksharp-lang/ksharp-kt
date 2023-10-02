package org.ksharp.semantics.scopes

import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.FunctionType
import org.ksharp.typesystem.types.TraitType
import org.ksharp.typesystem.types.toFunctionType

interface SemanticContext {

    val typeSystem: TypeSystem
    fun findFunctionType(name: String): FunctionType?

}

class TypeSystemSemanticContext(override val typeSystem: TypeSystem) : SemanticContext {
    override fun findFunctionType(name: String): FunctionType? =
        typeSystem["Decl__$name"].valueOrNull as? FunctionType;

}

class TraitSemanticContext(override val typeSystem: TypeSystem, private val trait: TraitType) : SemanticContext {
    override fun findFunctionType(name: String): FunctionType? =
        trait.methods[name]?.let {
            it.arguments.toFunctionType(it.attributes);
        }

}
