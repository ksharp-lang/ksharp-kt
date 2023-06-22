package org.ksharp.semantics.nodes

import InferenceErrorCode
import org.ksharp.common.*
import org.ksharp.module.FunctionInfo
import org.ksharp.semantics.scopes.SymbolTable
import org.ksharp.semantics.scopes.SymbolTableBuilder
import org.ksharp.semantics.scopes.Table
import org.ksharp.semantics.scopes.TableValue
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.newParameter

sealed interface TypePromise {
    val type: ErrorOrType
}

sealed class SemanticInfo {
    private var inferredType: ErrorOrType? = null

    fun hasInferredType(): Boolean = inferredType != null

    internal fun setInferredType(type: ErrorOrType) {
        inferredType = type
    }

    fun getInferredType(location: Location): ErrorOrType =
        inferredType ?: Either.Left(InferenceErrorCode.TypeNotInferred.new(location))
}

data class AbstractionSemanticInfo(
    val parameters: List<SemanticInfo>,
    val returnType: TypePromise? = null
) : SemanticInfo()

data class ApplicationSemanticInfo(var functionInfo: FunctionInfo? = null) : SemanticInfo()

data class EmptySemanticInfo(private val nothing: Unit = Unit) : SemanticInfo()

fun interface SymbolResolver {
    fun getSymbol(name: String): Symbol?
}

data class SymbolTableSemanticInfo(
    private val table: SymbolTable,
) : SemanticInfo(), SymbolResolver, Table<Symbol> {
    override fun getSymbol(name: String): Symbol? =
        table[name]?.first?.also { it.used.activate() }

    override fun get(name: String): TableValue<Symbol>? = table[name]
}

data class LetSemanticInfo(
    val table: SymbolTableBuilder,
) : SemanticInfo(), SymbolResolver, Table<Symbol> {
    override fun getSymbol(name: String): Symbol? =
        table[name]?.first?.also { it.used.activate() }

    override fun get(name: String): TableValue<Symbol>? = table[name]
}

data class MatchSemanticInfo(
    val table: SymbolTableBuilder,
) : SemanticInfo()

data class TypeSemanticInfo(
    override val type: ErrorOrType
) : SemanticInfo(), TypePromise

fun TypeSystem.getTypeSemanticInfo(name: String) =
    TypeSemanticInfo(get(name))

fun SemanticInfo.getType(location: Location): ErrorOrType =
    when (this) {
        is TypeSemanticInfo -> if (hasInferredType()) getInferredType(location) else type
        is Symbol -> if (hasInferredType()) getInferredType(location) else type.getType(location)
        else -> getInferredType(location)
    }

fun TypePromise.getType(location: Location): ErrorOrType =
    when (this) {
        is SemanticInfo -> this.cast<SemanticInfo>().getType(location)
        else -> type
    }

fun paramTypePromise() = TypeSemanticInfo(Either.Right(newParameter()))

fun Error.toTypePromise() = TypeSemanticInfo(Either.Left(this))
