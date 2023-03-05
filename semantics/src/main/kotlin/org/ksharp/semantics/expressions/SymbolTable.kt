package org.ksharp.semantics.expressions

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.semantics.errors.ErrorCollector
import org.ksharp.semantics.prelude.types.NumericType
import org.ksharp.semantics.scopes.Table
import org.ksharp.semantics.scopes.TableBuilder
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.types.ParametricType
import org.ksharp.typesystem.types.Type

enum class RecordSize(val size: Int) {
    Single(1),
    Double(2);
}

data class Symbol(
    val type: Type,
    val position: Int,
    val recordSize: RecordSize
)

data class SymbolSummary(
    val nextPosition: Int
)

class SymbolTableBuilder(parent: Table<Symbol, SymbolSummary>?, private val collector: ErrorCollector) :
    TableBuilder<Symbol, SymbolSummary>(parent, collector, "Variable") {

    private var summary = SymbolSummary(parent?.summary?.nextPosition ?: 0)

    private fun Type.numericType(): NumericType? =
        if ((this is ParametricType) && (this.params.size == 1)) {
            val variable = this.params.first()
            if (variable is NumericType) variable.cast<NumericType>()
            else null
        } else null

    fun register(
        name: String,
        value: ErrorOrType,
        location: Location
    ) = collector.collect(value).flatMap {
        val recordSize =
            it.numericType()?.type?.recordSize ?: RecordSize.Single
        val position = summary.nextPosition
        register(
            name, Symbol(
                it,
                position,
                recordSize
            ), location
        ).map { m ->
            summary = summary.copy(nextPosition = position + recordSize.size)
            m
        }
    }

    override fun summary(): SymbolSummary = summary

}

