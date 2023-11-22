package org.ksharp.semantics.scopes

import org.ksharp.common.*
import org.ksharp.semantics.errors.ErrorCollector

enum class TableErrorCode(override val description: String) : ErrorCode {
    AlreadyDefined("{classifier} already defined: {name}"),
}

open class TableBuilderImpl<Value>(
    private val parent: Table<Value>?,
    private val collector: ErrorCollector,
    private val classifier: String
) : TableBuilder<Value> {

    internal val table = mapBuilder<String, TableValue<Value>>()

    override fun get(name: String): TableValue<Value>? = table.get(name)
    override fun register(
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

    override fun build(): Table<Value> = TableImpl(parent, table.build())

}

data class TableImpl<Value>(
    private val parent: Table<Value>?,
    private val table: Map<String, TableValue<Value>>,
) : Table<Value> {
    override operator fun get(name: String): TableValue<Value>? = table[name] ?: parent?.get(name)

}
