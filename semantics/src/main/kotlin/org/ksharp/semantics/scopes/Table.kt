package org.ksharp.semantics.scopes

import org.ksharp.common.*
import org.ksharp.semantics.errors.ErrorCollector

enum class TableErrorCode(override val description: String) : ErrorCode {
    AlreadyDefined("{classifier} already defined: {name}"),
}

typealias TableValue<V> = Pair<V, Location>

open class TableBuilder<Value>(
    private val parent: Table<Value>?,
    private val collector: ErrorCollector,
    private val classifier: String
) {

    private val table = mapBuilder<String, TableValue<Value>>()

    fun register(
        name: String,
        value: Value,
        location: Location
    ): ErrorOrValue<Boolean> =
        if (this.table.containsKey(name) != true) {
            this.table.put(name, value to location)
            Either.Right(true)
        } else collector.collect(
            Either.Left(TableErrorCode.AlreadyDefined.new(location, "classifier" to classifier, "name" to name))
        )

    open fun build() = Table(parent, table.build())

}

class Table<Value>(
    private val parent: Table<Value>?,
    private val table: Map<String, TableValue<Value>>,
) {
    operator fun get(type: String): TableValue<Value>? = table[type] ?: parent?.get(type)
}