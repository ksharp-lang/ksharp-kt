package org.ksharp.semantics.nodes

import InferenceErrorCode
import org.ksharp.common.Either
import org.ksharp.common.Error
import org.ksharp.common.Location
import org.ksharp.common.new
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

class EmptySemanticInfo : SemanticInfo() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return javaClass == other?.javaClass
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

interface SymbolResolver {
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
        else -> getInferredType(location)
    }

fun paramTypePromise() = TypeSemanticInfo(Either.Right(newParameter()))

fun Error.toTypePromise() = TypeSemanticInfo(Either.Left(this))