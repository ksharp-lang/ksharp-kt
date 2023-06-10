package org.ksharp.lsp.actions

typealias ActionsGraphBuilder<Output> = PartialActionBuilder<Output>.() -> Unit

class ActionDependsOnBuilder(private val dependsOn: MutableList<Action<*, *>>) {

    operator fun Action<*, *>.unaryPlus() {
        dependsOn.add(this)
    }

}

class ActionTriggerBuilder<Output>(private val trigger: MutableList<Action<Output, *>>) {

    operator fun Action<Output, *>.unaryPlus() {
        trigger.add(this)
    }

}

class ActionsBuilder<Payload, Output>(
    private val id: String,
    private val whenCancelledOutput: Output
) {

    private var dependsOn = listOf<Action<*, *>>()
    private var trigger = listOf<Action<Output, *>>()
    private var execution: (ActionState, Payload) -> Output = { _, _ -> TODO() }

    fun dependsOn(builder: ActionDependsOnBuilder.() -> Unit) {
        val result = mutableListOf<Action<*, *>>()
        ActionDependsOnBuilder(result).apply(builder)
        dependsOn = result
    }

    fun trigger(builder: ActionTriggerBuilder<Output>.() -> Unit) {
        val result = mutableListOf<Action<Output, *>>()
        ActionTriggerBuilder(result).apply(builder)
        trigger = result
    }

    fun execution(execution: (ActionState, Payload) -> Output) {
        this.execution = execution
    }

    fun graphBuilder(builder: ActionsGraphBuilder<Output>) {
        PartialActionBuilder(this).builder()
    }

    fun build() = Action<Payload, Output>(
        id,
        whenCancelledOutput,
        trigger,
        dependsOn,
        execution
    )
}

class PartialActionBuilder<Output>(private val builder: ActionsBuilder<*, Output>) {

    fun dependsOn(builder: ActionDependsOnBuilder.() -> Unit) = this.builder.dependsOn(builder)

    fun trigger(builder: ActionTriggerBuilder<Output>.() -> Unit) = this.builder.trigger(builder)

}
