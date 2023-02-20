package org.ksharp.common

import org.ksharp.common.annotation.Mutable

typealias Factory<Value, Instance> = (value: Sequence<Value>) -> Instance
typealias Validation = Item<Error>

data class PartialBuilderResult<Instance>(
    val value: Instance,
    val errors: List<Error>
) {
    val isPartial: Boolean by lazy { errors.isNotEmpty() }
}

data class PartialItem<Value>(
    val item: Value?,
    val validations: Sequence<Error>
)

@Mutable
class PartialItemBuilder<Value> {
    private var item: Value? = null
    private val validations = lazySeqBuilder<Error>()

    fun value(item: Value) {
        this.item = item
    }

    fun validation(validation: Validation) = validations.add(validation)

    internal fun build() = PartialItem(item, validations.build())
}

@Mutable
class PartialBuilder<Value, Instance>(
    private val factory: Factory<Value, Instance>
) {
    private val state = listBuilder<PartialItem<Value>>()

    fun item(block: PartialItemBuilder<Value>.() -> Unit) {
        state.add(
            PartialItemBuilder<Value>()
                .apply(block)
                .build()
        )
    }

    fun build(): PartialBuilderResult<Instance> {
        val partialItems =
            state.build().asSequence()
                .map {
                    val errors = it.validations.toList()
                    if (errors.isEmpty()) {
                        it.item to null
                    } else null to errors
                }.toList()

        return PartialBuilderResult(
            factory(partialItems
                .asSequence()
                .mapNotNull { it.first }),
            partialItems.asSequence()
                .mapNotNull {
                    it.second
                }.flatten()
                .toList()
        )
    }

}

@Mutable
fun <ItemValue, Instance> partialBuilder(
    factory: Factory<ItemValue, Instance>
) = PartialBuilder(factory)