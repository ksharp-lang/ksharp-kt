package org.ksharp.semantics.scopes

import org.ksharp.common.*
import org.ksharp.semantics.errors.ErrorCollector

enum class TableErrorCode(override val description: String) : ErrorCode {
    AlreadyDefined("{classifier} already defined: {name}"),
}

open class TableBuilder<Value>(private val collector: ErrorCollector, private val classifier: String) {

    private val table = mapBuilder<String, Value>()
    fun register(
        name: String,
        value: Value,
        location: Location
    ): ErrorOrValue<Boolean> =
        if (this.table.containsKey(name) != true) {
            this.table.put(name, value)
            Either.Right(true)
        } else collector.collect(
            Either.Left(TableErrorCode.AlreadyDefined.new(location, "classifier" to classifier, "name" to name))
        )

    open fun build() = Table(table.build())

}

class Table<Value>(private val table: Map<String, Value>) {
    operator fun get(type: String): Value? = table[type]

}